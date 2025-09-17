package com.desapp.football_api.service;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.PlayerComplete;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class WhoScoredService {
    private final static String URL = "https://es.whoscored.com/players/";

    public Player scrapPlayerWithId(String id) throws java.io.IOException, InterruptedException {
        String url = URL + id + "/show";
        Document doc = fetchPlayerPageHtml(url);
        PlayerComplete player = new PlayerComplete();
        setPlayerData(player, doc);
//        setPlayerStats(player, doc);
        return player;
    }

    private Document fetchPlayerPageHtml(String url) throws java.io.IOException, InterruptedException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .header("Connection", "keep-alive")
                .header("Referer", "https://es.whoscored.com/")
//                .header("Cookie", "_fbp=fb.1.1758064465942.476798853467207590; _xpid=6325398004; _xpkey=kCSpXKfdThqha20bNjE_uvq4T__NKd9J; _adm-gpp=DBAA; _gid=GA1.2.1444691027.1758064509; tcf2cookie=CQX1pUAQX1pUAAJAGBESB8FsAP_gAEPgAAwILAFR_G__aWBBMCbnAIsEaQAHwAhAKEAAAAABAAAAQBAEJAAAAAAAAAAAAAAAAAAAgAAAAAAAAABQAAAAAAAAIAAAAEAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAAAAAAAQAgB-N7dr82dzyy4hHn3a5_2S1WJCdIYetDfv8ZBKT-9IEd_x8v4v4_F7pE2-eS1l_pGvp6D9-Yls_dBmx9_baffzPn__rk6CQAIACEAABAAAABBYAqP43_rS4IpgQI4BFghSAC-AEABQgAAAAAIAAACAIAhIAAAAAAAAAAAAAAAAAAEAAAAAAAAAAIAAAAAAAABAAAAAgAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAACAECWxnJte2yuWSTMEd-5VrzySqhMThTBgo71tjQICPV5Cnu6JWtFpH5ndImBSAGNv8o1MvW7D3Fs56tNeLr_PTpiX77kuenQSABAAQgAAgAAAAIAAAAA.f_wAD_wAAAAA; pubcv={}; acv=2~7.11.12.35.39.43.46.55.61.62.66.70.83.89.93.108.117.122.124.131.135.136.143.144.147.149.153.159.162.167.171.184.192.196.202.211.218.221.228.230.239.241.253.259.266.272.286.291.310.311.317.322.323.326.327.338.348.350.367.371.385.389.393.394.397.407.413.415.424.429.430.436.440.445.448.449.453.482.486.491.494.495.501.503.505.522.523.540.550.559.560.568.571.574.576.584.585.587.588.590.591.725.733.737.745.780.787.802.803.817.820.821.829.839.853.864.867.874.899.904.922.931.932.938.955.979.981.985.986.1003.1024.1027.1031.1033.1034.1040.1046.1051.1053.1067.1085.1092.1095.1097.1099.1107.1126.1127.1135.1143.1149.1152.1162.1166.1167.1170.1171.1178.1186.1188.1192.1201.1204.1205.1211.1215.1225.1226.1227.1230.1232.1236.1248.1252.1268.1276.1284.1286.1290.1301.1307.1312.1313.1317.1322.1329.1336.1344.1345.1356.1364.1365.1375.1403.1411.1415.1416.1419.1428.1440.1442.1449.1451.1455.1456.1465.1485.1495.1509.1512.1516.1525.1540.1542.1548.1555.1558.1564.1570.1577.1579.1583.1584.1591.1603.1608.1613.1616.1633.1638.1648.1651.1653.1665.1667.1669.1671.1677.1678.1682.1697.1699.1703.1712.1716.1720.1721.1722.1725.1732.1733.1735.1745.1750.1753.1765.1769.1776.1782.1786.1799.1800.1808.1810.1825.1827.1832.1834.1837.1838.1840.1842.1843.1844.1845.1859.1863.1866.1870.1875.1878.1880.1889.1896.1898.1899.1911.1917.1922.1929.1942.1943.1944.1962.1963.1964.1967.1968.1969.1978.1985.1987.1998.2003.2007.2010.2012.2013.2027.2035.2038.2039.2044.2047.2052.2056.2064.2068.2070.2072.2078.2079.2088.2090.2103.2107.2109.2113.2115.2124.2130.2133.2137.2140.2141.2145.2147.2150.2156.2166.2177.2179.2183.2186.2202.2205.2213.2216.2219.2220.2222.2225.2227.2234.2253.2262.2264.2271.2276.2279.2282.2290.2292.2299.2305.2309.2312.2315.2316.2325.2328.2331.2334.2335.2336.2337.2343.2354.2357.2358.2359.2366.2370.2373.2376.2377.2387.2392.2394.2400.2403.2405.2406.2407.2410.2411.2414.2416.2418.2422.2425.2427.2440.2447.2453.2459.2461.2462.2468.2472.2477.2481.2484.2486.2488.2492.2493.2496.2497.2498.2499.2504.2510.2511.2517.2526.2527.2531.2532.2534.2535.2542.2544.2552.2555.2563.2564.2567.2568.2569.2571.2572.2575.2577.2579.2583.2584.2589.2595.2596.2601.2604.2605.2608.2609.2610.2612.2614.2621.2628.2629.2633.2634.2636.2642.2643.2645.2646.2647.2650.2651.2652.2656.2657.2658.2660.2661.2663.2669.2670.2673.2677.2681.2682.2684.2686.2687.2690.2691.2695.2698.2704.2707.2710.2713.2714.2726.2729.2739.2767.2768.2770.2771.2772.2776.2778.2779.2784.2786.2787.2791.2792.2793.2797.2798.2801.2805.2808.2809.2812.2813.2816.2817.2818.2821.2822.2824.2827.2830.2831.2832.2834.2836.2838.2839.2840.2842.2844.2846.2847.2849.2850.2851.2852.2854.2856.2858.2860.2862.2863.2865.2867.2869.2873.2874.2875.2876.2878.2879.2880.2881.2882.2883.2884.2885.2886.2887.2888.2889.2891.2893.2894.2895.2897.2898.2900.2901.2904.2905.2908.2909.2911.2912.2913.2914.2916.2917.2918.2919.2920.2922.2923.2924.2926.2927.2929.2930.2931.2933.2939.2940.2941.2942.2945.2947.2949.2950.2956.2961.2962.2963.2964.2965.2966.2968.2969.2970.2973.2974.2975.2977.2979.2980.2981.2983.2985.2986.2987.2991.2993.2994.2995.2997.3000.3002.3003.3005.3008.3009.3010.3011.3012.3016.3017.3018.3019.3020.3023.3024.3025.3033.3034.3036.3037.3038.3043.3044.3045.3048.3050.3051.3052.3053.3055.3058.3059.3060.3061.3063.3065.3066.3068.3070.3072.3074.3075.3076.3077.3078.3089.3090.3093.3094.3095.3097.3099.3100.3101.3104.3106.3107.3108.3109.3111.3112.3116.3117.3118.3119.3120.3121.3124.3126.3127.3128.3130.3135.3136.3145.3149.3150.3151.3154.3155.3159.3162.3163.3165.3167.3172.3173.3174.3176.3177.3179.3180~dv.; ct=AR; sharedid=9eb5e307-9c9a-43c5-9c4e-5c17bfc0fff5; sharedid_cst=vSxlLDYsEQ%3D%3D; panoramaId_expiry=1758673353245; _cc_id=cab1e47d267128c1c619a78791d82e; panoramaId=b223ad6bcc165f4151e499a13571185ca02c82657e723d882be9e6cd20cdb6b2; connectId={\\\"ttl\\\":86400000,\\\"lastUsed\\\":1758068554556,\\\"lastSynced\\\":1758068554556}; cto_bidid=oUoDdF9zTUtrU0t0ZlFJaDlZOWljd0hoQ3RZRVNYR2pCT3FrdlAzajh3Z0c3VGpHTnBCWW1QdXJsSXJLMGhiZiUyRlFsUmIlMkJnMGczSmJ0RzdTRWZDcjR6NjVOQ1UxOVF4TCUyQkNOZFVoeW0wSkN5ciUyQmpjJTNE; cto_bundle=v4HeeV9UdyUyRmE5Q1o4UCUyQlJxZ3M2UEklMkJ6aWl5dVQ2S3NBN2pBV0FGUXVPV0VkZkN1elRBZ2VVcVFFMCUyRlBNQlh2VElLY29zNlltdk5PUDZRMHozRjhxeG44eXlYZ3hsQ1ZDRkxSRXZsT0lRVjRacHV4OURLSjE5QmlvZFVZeTd2SlpGT2lhczRHbFRCNG54aHNvRjBENG96U1VXVUMzTEslMkI3UWhUMkd3JTJCQ01FSEpvMkElM0Q; __gads=ID=af27e411d853f202:T=1758068557:RT=1758068557:S=ALNI_MY0en9zzB2Iw8JdencHrnXcR0OB1A; __gpi=UID=0000128e26242416:T=1758068557:RT=1758068557:S=ALNI_Maddo2pDC7cCGgS0uJlPoXzan40kw; __eoi=ID=1b4b831c6e341b0f:T=1758068557:RT=1758068557:S=AA-AfjbcT01Dx4XCDQ0V5TbYndu7; _awl=2.1758068561.5-508af309eaeebb46b162ff6bc5e2cd41-6763652d75732d6561737431-0; _awl=3.1758076119.5-de339ab0f386952d5d8c2fa032be4121-6763652d75732d6561737431-0; _ga=GA1.2.1838529801.1758064509; _ga_16TFZ6BTNV=GS2.1.s1758075747$o3$g1$t1758076119$j22$l0$h0")
                .timeout(30_000)
                .followRedirects(true)
                .get();
    }

    public void setPlayerData(PlayerComplete player, Document doc) {
        String className = "col12-lg-10 col12-m-10 col12-s-9 col12-xs-8";
        Element playerDataDiv = getElementByClassName(doc, className);

        List<String> values = new ArrayList<>();
        for (Element subDiv : playerDataDiv.select("div")) {
            String value = getSpecificData(subDiv);
            if (!Objects.equals(value, "")) values.add(value);
        }

        String fullname = values.getFirst();
        String team = values.get(1);
        String shirtNumber = values.get(2);
        String dateOfBirth = values.get(3);
        String nationality = values.get(5);
        String positions = values.get(6);

        player.setFullname(fullname);
        player.setShirtNumber(Integer.parseInt(shirtNumber));
        player.setTeam(team);
        player.setDateOfBirth(dateOfBirth);
        player.setNationality(nationality);
        player.setPositions(positions);

    }

    public void setPlayerStats(PlayerComplete player, Document doc) {
//        no funciona este method porque el tbody que necesitamos es cargado con javascript!!
        Element tbody = doc.selectFirst("tbody");
        validateElementExists(tbody, "tbody de estadísticas");
        Element thirdTr = tbody.select("tr").get(2);
        System.out.println("tbody: " + tbody);
        System.out.println("thirdTr: " + thirdTr);
        List<String> stats = new ArrayList<>();
        for (Element td : thirdTr.select("td")) {
            String stat = td.text().trim();
            if (stat.isEmpty()) {
                Element strong = td.selectFirst("strong");
                assert strong != null;
                stat = strong.text().trim();
            }
            stats.add(stat);
        }
        System.out.println(stats);

    }

    public String getSpecificData(Element element) {
        String text = element.ownText().trim();
        Element span = element.selectFirst("span");
        String spanText = span != null ? normalizeText(span.text()) : "";
        String result = "";

        if (spanText.startsWith("edad:")) {
            result = getTextFromChild(element, 1);
        }

        if (spanText.startsWith("equipo actual:")) {
            Element anchor = element.selectFirst("a");
            if (anchor != null) {
                return anchor.text();
            }
        }

        if (spanText.startsWith("nacionalidad:")) {
            result = getTextFromChild(element, 1);
        }

        if (spanText.startsWith("posiciones")) {
            Element secondSpan = element.children().get(1);
            if (secondSpan != null) {
                Elements firstChild = secondSpan.children();
                StringBuilder positions = new StringBuilder();
                for (Element pos : firstChild) {
                    positions.append(pos.text().trim()).append(" ");
                }
                return positions.toString().trim();
            }
        }

        return !result.isEmpty() ? result : text;
    }


    private void validateElementExists(Element element, String elementName) {
        if (element == null) {
            throw new IllegalStateException(elementName + " no encontrado en el documento");
        }
    }

    private Element getElementByClassName(Document doc, String className) {
        Element element = doc.selectFirst(className);
        if (element == null) {
            element = doc.selectFirst("div[class=\"" + className + "\"]");
        }
        validateElementExists(element, "Div con clase " + className);
        return element;
    }

    private Element getElementById(Document doc, String id) {
        Element element = doc.selectFirst("#" + id);
        validateElementExists(element, "Elemento con id " + id + " no encontrado en el documento");
        return element;
    }


    private String normalizeText(String text) {
        return text.replace("\n", "").replace("\t", "").trim().toLowerCase();
    }

    private String getTextFromChild(Element fatherElement, int numberOfChild) {
        Element child = fatherElement.children().get(numberOfChild);
        if (child != null) {
            return child.text().trim();
        }
        return "";
    }


}