// Registrar GSAP ScrollTrigger
gsap.registerPlugin(ScrollTrigger);

window.addEventListener('DOMContentLoaded', () => {
    /*********************
     * Config (endpoints de la API)
     *********************/

        // 🔧 Configuración de Base URL
        // Por defecto está vacío para usar rutas relativas
        // Cambiar a 'http://localhost:8080' cuando sea necesario

    const BASE_URL = '';
    // const BASE_URL = 'http://localhost:8080';

    const CONFIG = {
        // Helper para construir URLs completas

        buildUrl: (endpoint) => BASE_URL ? `${BASE_URL}${endpoint}` : endpoint,

        // Endpoints de la API
        get registerUrl() {
            return this.buildUrl('/api/users/register');
        },
        get loginUrl() {
            return this.buildUrl('/api/users/login');
        },
        get logoutUrl() {
            return this.buildUrl('/api/users/logout');
        },
        get meUrl() {
            return this.buildUrl('/api/users/me');
        },
        teamById: (id, type = 'current') => CONFIG.buildUrl(`/api/teams/${id}?type=${type}`),
        teamByName: (name, type = 'current') => CONFIG.buildUrl(`/api/teams/search?name=${encodeURIComponent(name)}&type=${type}`),
        playerById: (id, type = 'current') => CONFIG.buildUrl(`/api/players/${id}?type=${type}`),
        playerByName: (name, type = 'current') => CONFIG.buildUrl(`/api/players/search?name=${encodeURIComponent(name)}&type=${type}`)
    };

    /*********************
     * Utilidades DOM
     *********************/
    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => document.querySelectorAll(sel);

    /*********************
     * Inicializar animaciones GSAP
     *********************/
    function initAnimations() {
        // Animación del navbar
        let lastScrollY = window.scrollY;

        gsap.to('.navbar', {
            scrollTrigger: {
                trigger: 'body',
                start: 'top top',
                end: 'bottom bottom',
                onUpdate: self => {
                    const currentScrollY = self.scroll();
                    const navbar = $('.navbar');

                    if (currentScrollY > window.innerHeight * 0.8) {
                        navbar.classList.add('scrolled');
                    } else {
                        navbar.classList.remove('scrolled');
                    }

                    // if (currentScrollY > lastScrollY && currentScrollY > 200) {
                    //     gsap.to('.navbar', { y: -70, duration: 0.3 });
                    // } else {
                    //     gsap.to('.navbar', { y: 0, duration: 0.3 });
                    // }

                    lastScrollY = currentScrollY;
                }
            }
        });

        // Efecto de desaparición del video con scroll
        gsap.to('#home video', {
            opacity: 0,
            scale: 1.1,
            scrollTrigger: {
                trigger: '#home',
                start: 'top top',
                end: 'bottom top',
                scrub: 1
            }
        });

        // Animación del hero
        const tl = gsap.timeline();
        tl.from('.hero-label', {opacity: 0, y: 30, duration: 0.8, delay: 0.2})
            .from('.hero-line', {opacity: 0, y: 50, duration: 1, stagger: 0.2}, '-=0.4')
            .from('.hero-sub', {opacity: 0, y: 30, duration: 0.8}, '-=0.4')
            .from('.feature-item', {opacity: 0, y: 30, duration: 0.6, stagger: 0.1}, '-=0.4')
            .from('.hero-cta', {opacity: 0, scale: 0.9, duration: 0.6}, '-=0.2');

        // Animaciones de títulos con efecto de deslizamiento
        gsap.utils.toArray('.slide-in-left').forEach(element => {
            gsap.fromTo(element,
                {opacity: 0, x: -100},
                {
                    opacity: 1,
                    x: 0,
                    duration: 0.8,
                    ease: 'power2.out',
                    scrollTrigger: {
                        trigger: element,
                        start: 'top 85%',
                        end: 'bottom 10%',
                        toggleActions: 'play reverse play reverse',
                        onToggle: self => {
                            // Evitar que se desactive mientras esté visible
                            if (self.isActive) {
                                gsap.set(element, {opacity: 1, x: 0});
                            }
                        }
                    }
                }
            );
        });

        gsap.utils.toArray('.slide-in-right').forEach(element => {
            gsap.fromTo(element,
                {opacity: 0, x: 100},
                {
                    opacity: 1,
                    x: 0,
                    duration: 0.8,
                    ease: 'power2.out',
                    scrollTrigger: {
                        trigger: element,
                        start: 'top 85%',
                        end: 'bottom 10%',
                        toggleActions: 'play reverse play reverse',
                        onToggle: self => {
                            // Evitar que se desactive mientras esté visible
                            if (self.isActive) {
                                gsap.set(element, {opacity: 1, x: 0});
                            }
                        }
                    }
                }
            );
        });

        // Animaciones de scroll reveal para cards
        gsap.utils.toArray('.card').forEach(card => {
            gsap.fromTo(card,
                {opacity: 0, y: 50},
                {
                    opacity: 1,
                    y: 0,
                    duration: 0.4,
                    scrollTrigger: {
                        trigger: card,
                        start: 'top 80%',
                        end: 'bottom 20%',
                        toggleActions: 'play none none reverse'
                    }
                }
            );
        });

        // Animación de floating para elementos especiales
        gsap.to('.floating', {
            y: -10,
            duration: 2,
            ease: 'power2.inOut',
            yoyo: true,
            repeat: -1
        });
    }

    /*********************
     * Fetch helpers
     *********************/
    async function postJson(url, payload) {
        try {
            const resp = await fetch(url, {
                method: 'POST',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
            const text = await resp.text();
            try {
                return {ok: resp.ok, body: JSON.parse(text)};
            } catch {
                return {ok: resp.ok, body: text};
            }
        } catch (error) {
            return {ok: false, body: `Error: ${error.message}`};
        }
    }

    async function getJson(url) {
        try {
            const resp = await fetch(url, {credentials: 'include'});
            const text = await resp.text();
            try {
                return {ok: resp.ok, body: JSON.parse(text)};
            } catch {
                return {ok: resp.ok, body: text};
            }
        } catch (error) {
            return {ok: false, body: `Error: ${error.message}`};
        }
    }

    /*********************
     * Utilidades de UI
     *********************/
    function showResponse(containerId, data, isSuccess = true) {
        const container = $(containerId);
        if (!container) return;

        container.classList.remove('hidden');
        const responseText = typeof data === 'object' ? JSON.stringify(data, null, 2) : data;
        container.innerHTML = `<pre>${responseText}</pre>`;

        // Animación de aparición
        gsap.fromTo(container,
            {opacity: 0, y: 20},
            {opacity: 1, y: 0, duration: 0.5}
        );

        // Aplicar clase de estado
        const card = container.closest('.card');
        if (card) {
            card.classList.remove('success', 'error');
            card.classList.add(isSuccess ? 'success' : 'error');

            // Remover clase después de 3 segundos
            setTimeout(() => {
                card.classList.remove('success', 'error');
            }, 3000);
        }
    }

    /*********************
     * Auth Toggle
     *********************/
    $('#show-register')?.addEventListener('click', () => {
        $('#show-register').classList.add('active');
        $('#show-login').classList.remove('active');
        $('#form-register').classList.remove('hidden');
        $('#form-login').classList.add('hidden');
        $('#auth-response').classList.add('hidden');
    });

    $('#show-login')?.addEventListener('click', () => {
        $('#show-login').classList.add('active');
        $('#show-register').classList.remove('active');
        $('#form-login').classList.remove('hidden');
        $('#form-register').classList.add('hidden');
        $('#auth-response').classList.add('hidden');
    });

    /*********************
     * Registro
     *********************/
    $('#form-register')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = $('#reg-username').value.trim();
        const password = $('#reg-password').value;

        if (!username || !password) {
            showResponse('#auth-response', 'Por favor completa todos los campos', false);
            return;
        }

        const result = await postJson(CONFIG.registerUrl, {username, password});
        showResponse('#auth-response', result.body, result.ok);
    });

    /*********************
     * Login
     *********************/
    $('#form-login')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = $('#login-username').value.trim();
        const password = $('#login-password').value;

        if (!username || !password) {
            showResponse('#auth-response', 'Por favor completa todos los campos', false);
            return;
        }

        const result = await postJson(CONFIG.loginUrl, {username, password});
        showResponse('#auth-response', result.body, result.ok);
    });

    /*********************
     * Ver perfil (/users/me)
     *********************/
    $('#btn-me')?.addEventListener('click', async () => {
        const result = await getJson(CONFIG.meUrl);
        showResponse('#user-response', result.body, result.ok);
    });

    /*********************
     * Logout
     *********************/
    $('#btn-logout')?.addEventListener('click', async () => {
        const result = await postJson(CONFIG.logoutUrl, {});
        showResponse('#user-response', result.body, result.ok);
    });

    /*********************
     * Team unified search
     *********************/
    $('#form-team-unified')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const searchType = formData.get('searchType');
        const searchTerm = formData.get('searchTerm').trim();
        const dataType = formData.get('dataType');

        if (!searchTerm) {
            showResponse('#team-response', 'Por favor ingresa un término de búsqueda', false);
            return;
        }

        let result;
        if (searchType === 'id') {
            result = await getJson(CONFIG.teamById(searchTerm, dataType));
        } else {
            result = await getJson(CONFIG.teamByName(searchTerm, dataType));
        }

        showResponse('#team-response', result.body, result.ok);
    });

    /*********************
     * Player unified search
     *********************/
    $('#form-player-unified')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const searchType = formData.get('searchType');
        const searchTerm = formData.get('searchTerm').trim();
        const dataType = formData.get('dataType');

        if (!searchTerm) {
            showResponse('#player-response', 'Por favor ingresa un término de búsqueda', false);
            return;
        }

        let result;
        if (searchType === 'id') {
            result = await getJson(CONFIG.playerById(searchTerm, dataType));
        } else {
            result = await getJson(CONFIG.playerByName(searchTerm, dataType));
        }

        showResponse('#player-response', result.body, result.ok);
    });

    /*********************
     * Smooth scrolling para navegación
     *********************/
    let isNavigating = false;

    $$('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();

            if (isNavigating) return; // Prevenir clicks múltiples
            isNavigating = true;

            // Limpiar todas las clases active primero
            $$('.nav-link').forEach(l => l.classList.remove('active'));
            // Activar el link clickeado
            link.classList.add('active');

            const targetId = link.getAttribute('href').substring(1);
            const targetElement = $(`#${targetId}`);

            if (targetElement) {
                const navbarHeight = 70;
                const targetPosition = targetElement.offsetTop - navbarHeight;

                // Scroll suave personalizado
                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });

                // Resetear flag después del scroll
                setTimeout(() => {
                    isNavigating = false;
                }, 1000);
            } else {
                isNavigating = false;
            }
        });
    });

    /*********************
     * Active nav link management
     *********************/
    function updateActiveNavLink() {
        // No actualizar si estamos navegando
        if (isNavigating) return;

        const sections = $$('section');
        const navLinks = $$('.nav-link');
        const scrollPosition = window.scrollY + 100; // Offset para el navbar

        let activeSection = null;

        // Encontrar la sección activa
        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            const sectionBottom = sectionTop + section.offsetHeight;

            if (scrollPosition >= sectionTop && scrollPosition < sectionBottom) {
                activeSection = section;
            }
        });

        // Actualizar links activos
        navLinks.forEach(link => link.classList.remove('active'));

        if (activeSection) {
            const targetLink = $(`.nav-link[href="#${activeSection.id}"]`);
            if (targetLink) {
                targetLink.classList.add('active');
            }
        }
    }

    // Update active nav link on scroll con throttling
    let scrollTimer;
    window.addEventListener('scroll', () => {
        if (scrollTimer) {
            clearTimeout(scrollTimer);
        }
        scrollTimer = setTimeout(updateActiveNavLink, 50);
    });

    /*********************
     * Inicialización
     *********************/
    initAnimations();
    updateActiveNavLink();

    // Añadir clase para activar animaciones CSS
    document.body.classList.add('loaded');

    console.log('🚀 Football Analytics API Demo iniciado correctamente');
});