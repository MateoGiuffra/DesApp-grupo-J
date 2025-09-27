window.addEventListener('DOMContentLoaded', () => {
    /*********************
     * Config (tus endpoints reales)
     *********************/

    const CONFIG = {
        registerUrl: '/api/users/register',
        loginUrl: '/api/users/login',
        logoutUrl: '/api/users/logout',
        meUrl: '/api/users/me',
        teamById: (id) => `/api/teams/${id}`,
        teamByName: (name) => `/api/teams/search?name=${encodeURIComponent(name)}`,
        playerById: (id) => `/api/players/${id}`,
        playerByName: (name) => `/api/players/search?name=${encodeURIComponent(name)}`
    };

    /*********************
     * Utilidades DOM
     *********************/
    const $ = (sel) => document.querySelector(sel);

    /*********************
     * Fetch helpers
     *********************/
    async function postJson(url, payload) {
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
    }

    async function getJson(url) {
        const resp = await fetch(url, {credentials: 'include'});
        const text = await resp.text();
        try {
            return {ok: resp.ok, body: JSON.parse(text)};
        } catch {
            return {ok: resp.ok, body: text};
        }
    }

    /*********************
     * Registro
     *********************/
    $('#form-register')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = $('#reg-username').value.trim();
        const password = $('#reg-password').value;
        const result = await postJson(CONFIG.registerUrl, {username, password});
        const msg = $('#register-msg');
        msg.classList.remove('hidden');
        msg.textContent = JSON.stringify(result.body, null, 2);
    });

    /*********************
     * Login
     *********************/
    $('#form-login')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = $('#login-username').value.trim();
        const password = $('#login-password').value;
        const result = await postJson(CONFIG.loginUrl, {username, password});
        const msg = $('#login-msg');
        msg.classList.remove('hidden');
        msg.textContent = JSON.stringify(result.body, null, 2);
    });

    /*********************
     * Ver perfil (/users/me)
     *********************/
    $('#btn-me')?.addEventListener('click', async () => {
        const result = await getJson(CONFIG.meUrl);
        const msg = $('#me-msg');
        msg.classList.remove('hidden');
        msg.textContent = JSON.stringify(result.body, null, 2);
    });

    /*********************
     * Logout
     *********************/
    $('#btn-logout')?.addEventListener('click', async () => {
        const result = await postJson(CONFIG.logoutUrl, {});
        const msg = $('#logout-msg');
        msg.classList.remove('hidden');
        msg.textContent = JSON.stringify(result.body, null, 2);
    });

    /*********************
     * TEAM endpoints
     *********************/
    $('#form-team')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = $('#team-id').value.trim();
        const result = await getJson(CONFIG.teamById(id));
        const msg = $('#team-msg');
        msg.classList.remove('hidden');
        msg.textContent = JSON.stringify(result.body, null, 2);
    });

    $('#form-team-search')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = $('#team-name').value.trim();
        const result = await getJson(CONFIG.teamByName(name));
        const msg = $('#team-msg');
        msg.classList.remove('hidden');
        msg.textContent = JSON.stringify(result.body, null, 2);
    });

    /*********************
     * PLAYER endpoints
     *********************/
    $('#form-player')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = $('#player-id').value.trim();
        const result = await getJson(CONFIG.playerById(id));
        const msg = $('#player-msg');
        msg.classList.remove('hidden');
        msg.textContent = JSON.stringify(result.body, null, 2);
    });

    $('#form-player-search')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = $('#player-name').value.trim();
        const result = await getJson(CONFIG.playerByName(name));
        const msg = $('#player-msg');
        msg.classList.remove('hidden');
        msg.textContent = JSON.stringify(result.body, null, 2);
    });


    /*********************
     * Accessibility: keyboard support for nav
     *********************/
    document.getElementById('brand')?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') location.href = '#home';
    });


    function showLogin() {
        $('#section-register').classList.add('hidden');
        $('#section-login').classList.remove('hidden');
    }

    function showRegister() {
        $('#section-login').classList.add('hidden');
        $('#section-register').classList.remove('hidden');
    }

    $('#to-login')?.addEventListener('click', showLogin);
    $('#to-register')?.addEventListener('click', showRegister);


    /*********************
     * On-load: show register by default (como tu HTML original)
     *********************/

    // mostrar register por defecto
    showSectionInEntrega1('#section-register');
    // small initial scrolled state if starting mid-page
    if (window.scrollY > 40) navbar.classList.add('scrolled');

    /*********************
     * Detalles visuales
     *********************/
    window.addEventListener('scroll', function () {
        const navbar = document.querySelector('.navbar');
        if (window.scrollY > 20) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
});