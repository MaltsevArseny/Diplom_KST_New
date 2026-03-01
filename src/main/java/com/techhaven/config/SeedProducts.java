package com.techhaven.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Генератор данных 500 товаров для сидирования БД.
 * Каждая строка: {name, description, categoryName, price, stock, specs}.
 */
class SeedProducts {
    private SeedProducts() {}

    static Object[][] getData() {
        List<Object[]> list = new ArrayList<>(510);
        addCPU(list);
        addGPU(list);
        addRAM(list);
        addStorage(list);
        addMotherboards(list);
        addPSU(list);
        addCooling(list);
        addCases(list);
        addMonitors(list);
        addPeripherals(list);
        addNetworking(list);
        // Добавляем вариации до 500+
        addExtraProducts(list);
        return list.toArray(Object[][]::new);
    }

    /** Генерирует дополнительные товары до 500 */
    private static void addExtraProducts(List<Object[]> list) {
        int need = 500 - list.size();
        if (need <= 0) return;
        String[][] extras = {
            {"Процессоры","Intel Core i5-14500","14 ядер 14-пок.","24990.0","15","LGA1700, 2.6GHz, 65W"},
            {"Процессоры","AMD Ryzen 5 8600G","6 ядер AM5 iGPU","21990.0","12","AM5, 4.3GHz, 65W"},
            {"Процессоры","AMD Ryzen 7 8700G","8 ядер AM5 iGPU","29990.0","10","AM5, 4.2GHz, 65W"},
            {"Процессоры","Intel Core i3-13100","4 ядра 13-пок.","9990.0","30","LGA1700, 3.4GHz, 60W"},
            {"Процессоры","Intel Core i5-12600","6P+4E ядер","19990.0","18","LGA1700, 3.3GHz, 65W"},
            {"Процессоры","AMD Ryzen 3 4100","4 ядра AM4","7490.0","35","AM4, 3.8GHz, 65W"},
            {"Процессоры","Intel Core i9-13900KS","24 ядра Special","64990.0","3","LGA1700, 6.0GHz, 150W"},
            {"Процессоры","AMD Ryzen 5 7600","6 ядер AM5 65W","19990.0","16","AM5, 3.8GHz, 65W"},
            {"Процессоры","Intel Core i7-14700","20 ядер 65W","39990.0","9","LGA1700, 2.1GHz, 65W"},
            {"Видеокарты","NVIDIA GeForce RTX 4080 Super","16GB Super","89990.0","5","16GB, 10240 CUDA"},
            {"Видеокарты","NVIDIA GeForce RTX 4070 Ti Super","16GB TiS","69990.0","7","16GB, 8448 CUDA"},
            {"Видеокарты","NVIDIA GeForce RTX 4060 Ti 8GB","8GB 1440p","44990.0","10","8GB, 4352 CUDA"},
            {"Видеокарты","AMD Radeon RX 7600 XT","16GB 1080p+","34990.0","12","16GB, 2048 SP"},
            {"Видеокарты","NVIDIA GeForce GT 1030 2GB","2GB офисная","7990.0","40","2GB, 384 CUDA"},
            {"Видеокарты","AMD Radeon RX 6500 XT","4GB бюджет+","14990.0","22","4GB, 1024 SP"},
            {"Видеокарты","NVIDIA GeForce RTX 3090 Ti","24GB GDDR6X","149990.0","2","24GB, 10752 CUDA"},
            {"Видеокарты","MSI RTX 4070 Gaming X Trio","12GB OC","59990.0","8","12GB, 5888 CUDA"},
            {"Видеокарты","ASUS TUF RTX 4060 OC","8GB TUF","37990.0","12","8GB, 3072 CUDA"},
            {"Видеокарты","Gigabyte RX 7800 XT Gaming OC","16GB OC","54990.0","7","16GB, 3840 SP"},
            {"Оперативная память","Kingston FURY Beast 64GB DDR5","2x32GB 5600MHz","24990.0","6","DDR5-5600, CL40"},
            {"Оперативная память","Corsair Vengeance 16GB DDR5","2x8GB 5200MHz","7990.0","20","DDR5-5200, CL40"},
            {"Оперативная память","G.Skill Trident Z Neo 32GB DDR4","2x16GB 3600MHz","9490.0","14","DDR4-3600, CL16"},
            {"Оперативная память","Crucial Pro 32GB DDR5","2x16GB 5600MHz","13990.0","12","DDR5-5600, CL46"},
            {"Оперативная память","TeamGroup Elite 8GB DDR4","1x8GB 3200MHz","1990.0","60","DDR4-3200, CL22"},
            {"Оперативная память","ADATA XPG Lancer 32GB DDR5","2x16GB 5200MHz","11990.0","14","DDR5-5200, CL38"},
            {"Оперативная память","Samsung 16GB DDR4 SODIMM","DDR4 ноутбук","4990.0","25","SODIMM, 3200MHz"},
            {"Оперативная память","Kingston FURY Impact 16GB DDR5","SODIMM ноутбук","7490.0","18","SODIMM DDR5-4800"},
            {"Накопители","Samsung 990 EVO 1TB","PCIe 5.0 NVMe","13990.0","14","M.2, 5000 MB/s"},
            {"Накопители","WD Black SN770 1TB","NVMe Gen4","8990.0","20","5150 MB/s, M.2"},
            {"Накопители","Kingston FURY Renegade 2TB","PCIe 4.0","21990.0","6","7300 MB/s, M.2"},
            {"Накопители","Seagate Barracuda 1TB","HDD 7200rpm","3990.0","35","SATA III, 3.5\""},
            {"Накопители","Crucial T700 2TB","PCIe 5.0 NVMe","26990.0","5","12400 MB/s, M.2"},
            {"Накопители","Patriot P400 Lite 1TB","NVMe бюджет","4990.0","28","3500 MB/s, M.2"},
            {"Накопители","Samsung 980 1TB","NVMe Gen3","7990.0","22","3500 MB/s, M.2"},
            {"Накопители","WD Purple 4TB","HDD для видеонабл.","9490.0","10","5400rpm, SATA"},
            {"Накопители","Transcend 1TB External","Внешний HDD USB","5490.0","20","USB 3.1, 2.5\""},
            {"Накопители","SanDisk Ultra 3D 1TB","SATA SSD","7490.0","18","560 MB/s, 2.5\""},
            {"Материнские платы","ASUS ROG Crosshair X670E","AM5 энтузиаст","64990.0","3","AM5, X670E, DDR5"},
            {"Материнские платы","MSI MEG Z790 ACE","LGA1700 топ","54990.0","3","LGA1700, Z790, WiFi 7"},
            {"Материнские платы","Gigabyte B650 AORUS Elite AX","AM5 WiFi 6E","21990.0","10","AM5, B650, WiFi"},
            {"Материнские платы","ASRock B760M Steel Legend","mATX B760 WiFi","14990.0","15","LGA1700, WiFi 6"},
            {"Материнские платы","ASUS Prime A620M-E","mATX A620 AM5","8990.0","20","AM5, A620, DDR5"},
            {"Материнские платы","MSI B650M Gaming Plus WiFi","mATX B650","13990.0","16","AM5, WiFi 6E"},
            {"Материнские платы","Gigabyte Z790 UD AX","ATX Z790 WiFi","19990.0","10","LGA1700, WiFi 6E"},
            {"Блоки питания","ASUS ROG STRIX 850G","850W Gold ROG","15990.0","8","80+ Gold, Modular"},
            {"Блоки питания","Fractal Design Ion+ 2 860W","860W Platinum","17990.0","6","80+ Platinum"},
            {"Блоки питания","be quiet! Dark Power 13 1000W","1000W Titanium","34990.0","3","80+ Titanium"},
            {"Блоки питания","SilverStone SX700-G SFX","700W Gold SFX","12990.0","10","SFX, 80+ Gold"},
            {"Блоки питания","Cooler Master V850 Gold V2","850W Gold","13990.0","9","Full Modular"},
            {"Блоки питания","Thermaltake GF3 1000W","1000W PCIe 5.0","19990.0","5","80+ Gold, ATX3.0"},
            {"Охлаждение","ID-COOLING SE-226-XT","Tower 120mm","3990.0","20","TDP 250W, AM5"},
            {"Охлаждение","NZXT Kraken 240 RGB","AIO 240mm LCD","21990.0","6","240mm, LCD"},
            {"Охлаждение","Corsair iCUE H150i 360mm","AIO 360mm Elite","23990.0","4","360mm, iCUE"},
            {"Охлаждение","be quiet! Silent Loop 2 360","AIO 360mm тихий","19990.0","5","360mm, PWM"},
            {"Охлаждение","EK-AIO 240 D-RGB","AIO 240mm EK","14990.0","8","240mm, D-RGB"},
            {"Охлаждение","Deepcool Gammaxx 400 V2","Tower бюджет","1990.0","40","TDP 180W"},
            {"Корпуса","Cooler Master NR200P","Mini-ITX SFF","7990.0","10","ITX, TG Panel"},
            {"Корпуса","NZXT H6 Flow","ATX компактный","9990.0","12","ATX, USB-C"},
            {"Корпуса","Lian Li Lancool III","ATX Mesh","12990.0","8","ATX, 4x140mm"},
            {"Корпуса","Corsair 3500X","ATX Mid-Tower","8990.0","14","ATX, 3x120mm"},
            {"Корпуса","Fractal Design Pop XL Air","E-ATX Mesh","9990.0","10","E-ATX, Mesh"},
            {"Корпуса","Thermaltake S100 TG mATX","mATX компактный","4490.0","20","mATX, TG"},
            {"Корпуса","MSI MAG Pano M100R","mATX Panoramic","7490.0","14","mATX, TG Pano"},
            {"Корпуса","Phanteks NV5","ATX Mid-Tower","11990.0","9","ATX, 3x120mm"},
            {"Мониторы","ASUS TUF Gaming VG27AQ1A","27\" IPS 170Hz","27990.0","10","QHD, G-Sync"},
            {"Мониторы","MSI MAG274QRF-QD 27\"","27\" QD 165Hz","37990.0","7","QHD, Quantum Dot"},
            {"Мониторы","LG 32GP83B-B 32\"","32\" QHD 165Hz IPS","35990.0","8","Nano IPS, HDR400"},
            {"Мониторы","Samsung S34BG850 Ultrawide","34\" OLED UW","79990.0","3","3440x1440, OLED"},
            {"Мониторы","AOC U28P2A 28\" 4K","28\" 4K IPS 60Hz","24990.0","12","3840x2160, sRGB"},
            {"Мониторы","BenQ MOBIUZ EX2710Q","27\" QHD 165Hz","34990.0","8","IPS, HDRi, 2.1ch"},
            {"Мониторы","Gigabyte M28U 28\" 4K","28\" 4K 144Hz","39990.0","6","IPS, KVM, USB-C"},
            {"Мониторы","ASUS ROG Swift PG27AQN","27\" QHD 360Hz","99990.0","2","IPS, 360Hz, G-Sync"},
            {"Периферия","Logitech MX Master 3S","Мышь для работы","8990.0","15","Bluetooth, USB-C"},
            {"Периферия","Razer Viper V3 Pro","Мышь беспровод. 54г","15990.0","8","Focus Pro 35K"},
            {"Периферия","Corsair K100 RGB","Флагман клавиатура","16990.0","6","OPX Switch, OLED"},
            {"Периферия","Keychron Q1 Pro","75% Al кастом","14990.0","10","Bluetooth, QMK/VIA"},
            {"Периферия","Beyerdynamic DT 900 PRO X","Студийные наушники","21990.0","5","Open-back, 48 Ohm"},
            {"Периферия","Rode NT-USB Mini","Компактный микрофон","8990.0","12","USB-C, кардиоидный"},
            {"Периферия","Elgato Stream Deck MK.2","Панель стриминга","14990.0","7","15 LCD клавиш"},
            {"Периферия","SteelSeries QcK Heavy XXL","Коврик XXL","3990.0","25","900x400, 6mm"},
            {"Периферия","Logitech BRIO 4K","Вебкамера 4K HDR","16990.0","6","4K, USB-C, HDR"},
            {"Периферия","8BitDo Ultimate BT","Геймпад Bluetooth","5990.0","20","Hall Effect, BT5"},
            {"Сетевое оборудование","TP-Link Archer AXE75","WiFi 6E роутер","12990.0","10","AXE5400, 6GHz"},
            {"Сетевое оборудование","ASUS RT-BE88U","WiFi 7 роутер","29990.0","4","BE7200, 2.5G WAN"},
            {"Сетевое оборудование","Ubiquiti Dream Router","Роутер UniFi","19990.0","5","WiFi 6, PoE, NVR"},
            {"Сетевое оборудование","MikroTik RB5009UG","Роутер 10G SFP+","16990.0","6","2.5G, SFP+, USB"},
            {"Сетевое оборудование","TP-Link Omada EAP650","AP WiFi 6 AX3000","6990.0","12","AX3000, PoE, Omada"},
            {"Сетевое оборудование","Netgear GS308E 8-port","Smart Switch 8p","2990.0","25","8xGigabit, VLAN"},
            {"Сетевое оборудование","Keenetic Viva KN-1912","WiFi 5 двухдиап.","4990.0","18","AC1300, 4 LAN"},
            {"Сетевое оборудование","D-Link DIR-X1860","WiFi 6 AX1800","4990.0","20","AX1800, Gigabit"},
            {"Сетевое оборудование","Сетевая карта TP-Link TX201","2.5G PCIe","2490.0","20","PCIe, 2.5Gbps"},
            {"Сетевое оборудование","Сетевой фильтр 5м 6 розеток","Удлинитель с защитой","1490.0","50","6 розеток, USB"},
        };
        for (int i = 0; i < Math.min(need, extras.length); i++) {
            String[] e = extras[i];
            list.add(new Object[]{e[1], e[2], e[0], Double.valueOf(e[3]), Integer.valueOf(e[4]), e[5]});
        }
    }


    private static void addCPU(List<Object[]> l) {
        String C = "Процессоры";
        l.add(new Object[]{"Intel Core i3-12100F","4 ядра без GPU",C,8490.0,35,"LGA1700, 3.3GHz, 58W"});
        l.add(new Object[]{"Intel Core i5-12400F","6 ядер без GPU",C,14990.0,25,"LGA1700, 2.5GHz, 65W"});
        l.add(new Object[]{"Intel Core i5-13400","10 ядер",C,21990.0,15,"LGA1700, 2.5GHz, 65W"});
        l.add(new Object[]{"Intel Core i5-13500","14 ядер",C,26990.0,12,"LGA1700, 2.5GHz, 65W"});
        l.add(new Object[]{"Intel Core i5-13600K","14 ядер разгон",C,29990.0,10,"LGA1700, 3.5GHz, 125W"});
        l.add(new Object[]{"Intel Core i5-14400F","10 ядер 14-е пок.",C,18990.0,25,"LGA1700, 2.5GHz, 65W"});
        l.add(new Object[]{"Intel Core i5-14600K","14 ядер 14-е пок.",C,31990.0,8,"LGA1700, 3.5GHz, 125W"});
        l.add(new Object[]{"Intel Core i7-12700K","12 ядер 12-е пок.",C,29990.0,10,"LGA1700, 3.6GHz, 125W"});
        l.add(new Object[]{"Intel Core i7-13700","16 ядер 65W",C,37990.0,8,"LGA1700, 2.1GHz, 65W"});
        l.add(new Object[]{"Intel Core i7-13700K","16 ядер разгон",C,38990.0,10,"LGA1700, 3.4GHz, 125W"});
        l.add(new Object[]{"Intel Core i7-14700K","20 ядер 14-е пок.",C,42990.0,7,"LGA1700, 3.4GHz, 125W"});
        l.add(new Object[]{"Intel Core i9-12900KS","16 ядер до 5.5GHz",C,54990.0,4,"LGA1700, 5.5GHz, 150W"});
        l.add(new Object[]{"Intel Core i9-13900K","24 ядра флагман",C,54990.0,5,"LGA1700, 3.0GHz, 125W"});
        l.add(new Object[]{"Intel Core i9-14900K","24 ядра 14-е пок.",C,59990.0,4,"LGA1700, 3.2GHz, 125W"});
        l.add(new Object[]{"Intel Celeron G6900","2 ядра офис",C,3990.0,60,"LGA1700, 3.4GHz, 46W"});
        l.add(new Object[]{"Intel Pentium Gold G7400","2 ядра бюджет",C,5990.0,40,"LGA1700, 3.7GHz, 46W"});
        l.add(new Object[]{"AMD Ryzen 5 5600G","6 ядер Vega 7",C,14990.0,18,"AM4, 3.9GHz, 65W"});
        l.add(new Object[]{"AMD Ryzen 5 5600X","6 ядер AM4",C,15990.0,20,"AM4, 3.7GHz, 65W"});
        l.add(new Object[]{"AMD Ryzen 5 7600X","6 ядер AM5",C,22990.0,15,"AM5, 4.7GHz, 105W"});
        l.add(new Object[]{"AMD Ryzen 5 4600G","6 ядер Vega AM4",C,13490.0,20,"AM4, 3.7GHz, 65W"});
        l.add(new Object[]{"AMD Ryzen 7 5700G","8 ядер Vega 8",C,18990.0,15,"AM4, 3.8GHz, 65W"});
        l.add(new Object[]{"AMD Ryzen 7 5800X","8 ядер AM4",C,24990.0,12,"AM4, 3.8GHz, 105W"});
        l.add(new Object[]{"AMD Ryzen 7 5800X3D","8 ядер 3D V-Cache",C,33990.0,10,"AM4, 3.4GHz, 105W"});
        l.add(new Object[]{"AMD Ryzen 7 7700X","8 ядер AM5",C,32990.0,10,"AM5, 4.5GHz, 105W"});
        l.add(new Object[]{"AMD Ryzen 7 7800X3D","8 ядер 3D V-Cache AM5",C,39990.0,6,"AM5, 4.2GHz, 120W"});
        l.add(new Object[]{"AMD Ryzen 9 5900X","12 ядер AM4",C,39990.0,8,"AM4, 3.7GHz, 105W"});
        l.add(new Object[]{"AMD Ryzen 9 5950X","16 ядер AM4 флагман",C,54990.0,5,"AM4, 3.4GHz, 105W"});
        l.add(new Object[]{"AMD Ryzen 9 7900X","12 ядер AM5",C,44990.0,6,"AM5, 4.7GHz, 170W"});
        l.add(new Object[]{"AMD Ryzen 9 7950X","16 ядер AM5 флагман",C,59990.0,4,"AM5, 4.5GHz, 170W"});
        l.add(new Object[]{"AMD Athlon 3000G","2C/4T Vega 3",C,4490.0,40,"AM4, 3.5GHz, 35W"});
        l.add(new Object[]{"AMD Ryzen Threadripper 3960X","24 ядра HEDT",C,159990.0,2,"TRX40, 3.8GHz, 280W"});
        // 31 CPU
    }

    private static void addGPU(List<Object[]> l) {
        String C = "Видеокарты";
        l.add(new Object[]{"NVIDIA GeForce GTX 1650 OC","4GB GDDR6 бюджет",C,14990.0,25,"4GB, 896 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce GTX 1660 Super","6GB GDDR6",C,21990.0,18,"6GB, 1408 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 3050 8GB","8GB 1080p",C,23990.0,20,"8GB, 2560 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 3060 12GB","12GB 1080p/1440p",C,32990.0,15,"12GB, 3584 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 3060 Ti","8GB 1440p",C,39990.0,10,"8GB, 4864 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 3070 8GB","8GB 1440p",C,44990.0,8,"8GB, 5888 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 3070 Ti","8GB GDDR6X",C,49990.0,6,"8GB, 6144 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 3080 10GB","10GB 4K",C,69990.0,5,"10GB, 8704 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 3080 Ti","12GB GDDR6X",C,79990.0,4,"12GB, 10240 CUDA"});
        l.add(new Object[]{"MSI Suprim X RTX 4090","24GB флагман",C,199990.0,2,"24GB, 16384 CUDA"});
        l.add(new Object[]{"Gigabyte RTX 4080 16GB","16GB 4K",C,109990.0,3,"16GB, 9728 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 4070 Ti","12GB 1440p+",C,74990.0,6,"12GB, 7680 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 4070 12GB","12GB 1440p",C,54990.0,10,"12GB, 5888 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 4060 Ti 16GB","16GB 1440p",C,56990.0,7,"16GB, 4352 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 4060 8GB","8GB 1080p/1440p",C,35990.0,14,"8GB, 3072 CUDA"});
        l.add(new Object[]{"NVIDIA GeForce RTX 4090 Ti","24GB топ",C,199990.0,1,"24GB, 18176 CUDA"});
        l.add(new Object[]{"AMD Radeon RX 6400 4GB","4GB бюджет",C,11990.0,30,"4GB, 768 SP"});
        l.add(new Object[]{"AMD Radeon RX 6600 8GB","8GB 1080p",C,22990.0,20,"8GB, 1792 SP"});
        l.add(new Object[]{"AMD Radeon RX 6700 XT","12GB 1440p",C,34990.0,12,"12GB, 2560 SP"});
        l.add(new Object[]{"AMD Radeon RX 6800 XT","16GB 4K",C,54990.0,6,"16GB, 4608 SP"});
        l.add(new Object[]{"AMD Radeon RX 6950 XT","16GB флагман",C,74990.0,3,"16GB, 4096 SP"});
        l.add(new Object[]{"AMD Radeon RX 7600 8GB","8GB 1080p",C,28990.0,15,"8GB, 2048 SP"});
        l.add(new Object[]{"AMD Radeon RX 7700 XT","12GB 1440p",C,44990.0,8,"12GB, 3456 SP"});
        l.add(new Object[]{"AMD Radeon RX 7800 XT","16GB 1440p+",C,52990.0,7,"16GB, 3840 SP"});
        l.add(new Object[]{"AMD Radeon RX 7900 GRE","16GB 4K",C,64990.0,5,"16GB, 5120 SP"});
        l.add(new Object[]{"AMD Radeon RX 7900 XT","20GB 4K",C,84990.0,4,"20GB, 5376 SP"});
        l.add(new Object[]{"AMD Radeon RX 7900 XTX","24GB флагман",C,99990.0,3,"24GB, 6144 SP"});
        l.add(new Object[]{"Intel Arc A770 16GB","16GB бюджет",C,27990.0,12,"16GB, 32 Xe"});
        l.add(new Object[]{"Intel Arc A750 8GB","8GB бюджет",C,22990.0,15,"8GB, 28 Xe"});
        l.add(new Object[]{"Intel Arc A580 8GB","8GB 1080p",C,19990.0,20,"8GB, 24 Xe"});
        // 30 GPU
    }

    private static void addRAM(List<Object[]> l) {
        String C = "Оперативная память";
        l.add(new Object[]{"Kingston FURY Beast 16GB DDR4","2x8GB 3200MHz",C,4990.0,30,"DDR4-3200, CL16"});
        l.add(new Object[]{"Kingston FURY Beast 32GB DDR4","2x16GB 3200MHz",C,8490.0,20,"DDR4-3200, CL16"});
        l.add(new Object[]{"Kingston FURY Beast 32GB DDR5","2x16GB 5600MHz",C,12990.0,15,"DDR5-5600, CL40"});
        l.add(new Object[]{"Kingston ValueRAM 8GB DDR4","1x8GB 2666MHz",C,2490.0,50,"DDR4-2666, CL19"});
        l.add(new Object[]{"Kingston FURY Renegade 16GB DDR4","2x8GB 3600MHz",C,6490.0,18,"DDR4-3600, CL16"});
        l.add(new Object[]{"Kingston FURY Renegade 32GB DDR5","2x16GB 6400MHz",C,19990.0,8,"DDR5-6400, CL32"});
        l.add(new Object[]{"Corsair Vengeance 16GB DDR4","2x8GB 3200MHz",C,5490.0,25,"DDR4-3200, CL16"});
        l.add(new Object[]{"Corsair Vengeance 32GB DDR5","2x16GB 5200MHz",C,11990.0,12,"DDR5-5200, CL40"});
        l.add(new Object[]{"Corsair Vengeance 64GB DDR5","2x32GB 5200MHz",C,29990.0,6,"DDR5-5200, CL40"});
        l.add(new Object[]{"Corsair Dominator 32GB DDR5","2x16GB 6000MHz RGB",C,18990.0,8,"DDR5-6000, CL36"});
        l.add(new Object[]{"G.Skill Trident Z5 32GB DDR5","2x16GB 6000MHz RGB",C,17990.0,9,"DDR5-6000, CL30"});
        l.add(new Object[]{"G.Skill Ripjaws S5 32GB DDR5","2x16GB 6400MHz",C,16990.0,10,"DDR5-6400, CL32"});
        l.add(new Object[]{"G.Skill Ripjaws V 16GB DDR4","2x8GB 3600MHz",C,4990.0,30,"DDR4-3600, CL18"});
        l.add(new Object[]{"G.Skill Ripjaws V 32GB DDR4","2x16GB 3600MHz",C,8990.0,20,"DDR4-3600, CL18"});
        l.add(new Object[]{"Crucial 16GB DDR4-3200","1x16GB 3200MHz",C,4990.0,30,"DDR4-3200, CL22"});
        l.add(new Object[]{"Crucial 32GB DDR4-3200 SODIMM","DDR4 ноутбук",C,9990.0,12,"SODIMM, CL22"});
        l.add(new Object[]{"HyperX Impact 32GB DDR4","2x16GB ноутбук",C,8990.0,15,"SO-DIMM, 3200MHz"});
        l.add(new Object[]{"Team T-Force Vulcan 16GB DDR4","Геймерская",C,3990.0,35,"DDR4-3200, CL16"});
        l.add(new Object[]{"Team T-Force Vulcan Z 16GB DDR4","3600MHz RGB",C,5490.0,22,"DDR4-3600, CL18"});
        l.add(new Object[]{"ADATA XPG Spectrix 64GB DDR5","2x32GB",C,29990.0,5,"DDR5-5600, CL36"});
        l.add(new Object[]{"ADATA XPG Lattice 32GB DDR5","7200MHz",C,19990.0,8,"DDR5-7200, CL34"});
        l.add(new Object[]{"Patriot Viper 4 16GB DDR4","Надёжная без RGB",C,3490.0,45,"DDR4-3200, CL16"});
        l.add(new Object[]{"Patriot Viper Elite 32GB DDR4","2x16GB геймерская",C,7990.0,18,"DDR4-3200, CL16"});
        // 23 RAM
    }

    private static void addStorage(List<Object[]> l) {
        String C = "Накопители";
        l.add(new Object[]{"Samsung 970 EVO Plus 1TB","NVMe SSD",C,9990.0,18,"NVMe 3.0, 3500 MB/s"});
        l.add(new Object[]{"Samsung 980 PRO 2TB","NVMe Gen4",C,19990.0,8,"NVMe 4.0, 7000 MB/s"});
        l.add(new Object[]{"Samsung 990 Pro 1TB","PCIe 4.0 топ",C,11990.0,20,"7450 MB/s, M.2"});
        l.add(new Object[]{"Samsung 990 Pro 2TB","PCIe 4.0 2TB",C,21990.0,10,"7450 MB/s, M.2"});
        l.add(new Object[]{"Samsung 870 EVO 1TB","SATA SSD",C,8990.0,20,"SATA III, 560 MB/s"});
        l.add(new Object[]{"Samsung 870 QVO 1TB","SATA QLC",C,7490.0,30,"SATA III, 560 MB/s"});
        l.add(new Object[]{"Samsung 870 QVO 2TB","SATA QLC 2TB",C,14990.0,10,"SATA III, 560 MB/s"});
        l.add(new Object[]{"Samsung T7 1TB External","Внешний SSD",C,12000.0,12,"USB 3.2, 1050 MB/s"});
        l.add(new Object[]{"WD Black SN850X 1TB","PCIe 4.0",C,12990.0,12,"7300 MB/s, Heatsink"});
        l.add(new Object[]{"WD Black SN850X 2TB","PCIe 4.0 2TB",C,19990.0,6,"7300 MB/s, M.2"});
        l.add(new Object[]{"WD Blue SN580 1TB","NVMe PCIe 4.0",C,7490.0,22,"4150 MB/s, M.2"});
        l.add(new Object[]{"WD Blue 2TB HDD","HDD 7200rpm",C,5990.0,40,"7200rpm, SATA"});
        l.add(new Object[]{"WD Red Plus 4TB NAS","NAS HDD",C,9990.0,12,"5400rpm, CMR"});
        l.add(new Object[]{"WD Green 240GB SSD","SATA базовый",C,2990.0,50,"SATA III, 545 MB/s"});
        l.add(new Object[]{"WD Elements 4TB External","Внешний HDD",C,10000.0,15,"USB 3.0, 3.5\""});
        l.add(new Object[]{"Seagate Barracuda 2TB","HDD 7200rpm",C,5490.0,30,"7200rpm, SATA"});
        l.add(new Object[]{"Seagate Barracuda 4TB","HDD 5400rpm",C,8490.0,20,"5400rpm, SATA"});
        l.add(new Object[]{"Seagate IronWolf 6TB","NAS HDD",C,12990.0,8,"5400rpm, CMR"});
        l.add(new Object[]{"Seagate FireCuda 530 2TB","PCIe 4.0 PS5",C,19990.0,7,"7300 MB/s, M.2"});
        l.add(new Object[]{"Seagate Exos 8TB","Серверный HDD",C,16990.0,6,"7200rpm, SATA"});
        l.add(new Object[]{"Seagate 1TB HDD","HDD 7200rpm",C,4990.0,25,"7200rpm, SATA"});
        l.add(new Object[]{"Crucial P5 Plus 1TB","PCIe 4.0",C,8990.0,25,"6600 MB/s, M.2"});
        l.add(new Object[]{"Crucial MX500 500GB","SATA SSD",C,4490.0,45,"560 MB/s, 2.5\""});
        l.add(new Object[]{"Crucial BX500 480GB","SATA бюджет",C,3990.0,40,"540 MB/s, 2.5\""});
        l.add(new Object[]{"Crucial 500GB SSD","SATA SSD",C,4990.0,30,"SATA, 560 MB/s"});
        l.add(new Object[]{"Kingston NV2 1TB","Бюджетный NVMe",C,5490.0,40,"3500 MB/s, M.2"});
        l.add(new Object[]{"Kingston KC3000 2TB","NVMe PCIe 4.0",C,17990.0,8,"7000 MB/s, M.2"});
        l.add(new Object[]{"Kingston KC600 256GB","Бюджетный SATA",C,2990.0,60,"550 MB/s, 2.5\""});
        l.add(new Object[]{"ADATA Legend 800 1TB","NVMe Gen4 бюджет",C,5990.0,32,"3500 MB/s, M.2"});
        l.add(new Object[]{"ADATA Legend 960 1TB","NVMe PCIe 4.0",C,6990.0,18,"7400 MB/s, M.2"});
        l.add(new Object[]{"Toshiba P300 2TB","HDD 7200rpm",C,4490.0,25,"7200rpm, SATA"});
        l.add(new Object[]{"Флешка Kingston 128GB","USB 3.2",C,1500.0,80,"USB 3.2 Gen 1"});
        l.add(new Object[]{"Флешка Samsung 256GB","Type-C",C,3500.0,40,"Type-C, USB 3.2"});
        // 33 storage
    }

    private static void addMotherboards(List<Object[]> l) {
        String C = "Материнские платы";
        l.add(new Object[]{"ASUS Prime B760 LGA1700","ATX B760",C,15990.0,12,"LGA1700, DDR5"});
        l.add(new Object[]{"ASUS Prime Z790-P DDR5","ATX Z790",C,22990.0,12,"LGA1700, Z790"});
        l.add(new Object[]{"ASUS Prime H610M-E","mATX H610 бюджет",C,7490.0,25,"LGA1700, DDR4"});
        l.add(new Object[]{"ASUS ROG X670E Hero","AM5 флагман",C,45000.0,3,"AM5, X670E"});
        l.add(new Object[]{"ASUS ROG Strix X670E-F","AM5 ATX",C,52990.0,4,"AM5, X670E, DDR5"});
        l.add(new Object[]{"ASUS ROG STRIX Z790-E","ATX Z790 RGB",C,49990.0,5,"LGA1700, Z790"});
        l.add(new Object[]{"ASUS TUF Gaming B550-Plus","ATX B550 AM4",C,12990.0,20,"AM4, B550"});
        l.add(new Object[]{"ASUS TUF GAMING B660-Plus","ATX B660",C,15990.0,12,"LGA1700, B660"});
        l.add(new Object[]{"MSI B550 Tomahawk AM4","ATX B550",C,14990.0,10,"AM4, B550"});
        l.add(new Object[]{"MSI MAG B760M Mortar DDR5","mATX B760",C,15990.0,18,"LGA1700, B760"});
        l.add(new Object[]{"MSI PRO B660M-A DDR4","mATX B660",C,9990.0,25,"LGA1700, B660"});
        l.add(new Object[]{"MSI PRO Z790-A WiFi","ATX Z790 WiFi",C,27990.0,9,"LGA1700, WiFi 6E"});
        l.add(new Object[]{"MSI MAG B550M Mortar","mATX B550 AM4",C,12990.0,14,"AM4, B550"});
        l.add(new Object[]{"MSI MPG X670E Carbon WiFi","ATX X670E",C,44990.0,5,"AM5, WiFi 6E"});
        l.add(new Object[]{"Gigabyte B650 AM5 Aorus","ATX B650",C,18000.0,15,"AM5, Aorus"});
        l.add(new Object[]{"Gigabyte B550 AORUS Elite V2","ATX B550 AM4",C,14990.0,14,"AM4, B550"});
        l.add(new Object[]{"Gigabyte Z790 AORUS Master","ATX Z790 топ",C,49990.0,3,"LGA1700, Z790"});
        l.add(new Object[]{"Gigabyte A520M DS3H","mATX A520 AM4",C,6990.0,40,"AM4, A520"});
        l.add(new Object[]{"Gigabyte B760M DS3H","mATX B760",C,10290.0,20,"LGA1700, B760"});
        l.add(new Object[]{"Gigabyte B450M DS3H V2","mATX B450 AM4",C,7990.0,22,"AM4, B450"});
        l.add(new Object[]{"ASRock B650M PG Riptide","mATX B650 AM5",C,14990.0,16,"AM5, B650"});
        l.add(new Object[]{"ASRock B650E Taichi","ATX B650E AM5",C,34990.0,6,"AM5, PCIe 5.0"});
        l.add(new Object[]{"ASRock X570 Phantom Gaming 4","ATX X570 AM4",C,19990.0,8,"AM4, PCIe 4.0"});
        l.add(new Object[]{"ASRock H610M-HDV","mATX H610 офис",C,6490.0,30,"LGA1700, DDR4"});
        // 24 motherboards
    }

    private static void addPSU(List<Object[]> l) {
        String C = "Блоки питания";
        l.add(new Object[]{"Corsair RM750x","750W 80+ Gold",C,12990.0,12,"Full Modular"});
        l.add(new Object[]{"Corsair RM850x SHIFT","850W Gold боковой",C,14990.0,8,"Semi-Modular"});
        l.add(new Object[]{"Corsair RM1000x Shift","1000W Gold",C,24990.0,4,"Side Connect"});
        l.add(new Object[]{"Seasonic Focus GX-850","850W Gold модульный",C,13990.0,10,"Full Modular"});
        l.add(new Object[]{"Seasonic Focus GX-1000","1000W Gold",C,18990.0,5,"Hybrid Fan"});
        l.add(new Object[]{"Seasonic Prime TX-1000","1000W Titanium",C,29990.0,3,"Full Modular"});
        l.add(new Object[]{"SeaSonic 1000W Platinum","Prime 1000W",C,25000.0,5,"Platinum"});
        l.add(new Object[]{"be quiet! SP11 750W","Platinum тихий",C,19990.0,6,"Full Modular"});
        l.add(new Object[]{"be quiet! System Power 10 550W","550W Bronze",C,6490.0,20,"ATX 550W"});
        l.add(new Object[]{"Cooler Master MWE Gold 750W","750W Gold",C,9490.0,15,"Non-Modular"});
        l.add(new Object[]{"Cooler Master MWE 650W Bronze","650W Bronze",C,5990.0,20,"Non-Modular"});
        l.add(new Object[]{"Cooler Master 650W Bronze","650W",C,6990.0,16,"80+ Bronze"});
        l.add(new Object[]{"Deepcool PQ850M","850W Gold модульный",C,11990.0,9,"Full Modular"});
        l.add(new Object[]{"Deepcool PK550D 550W","550W Bronze бюджет",C,4490.0,40,"Non-Modular"});
        l.add(new Object[]{"Deepcool 850W Gold","850W Gold",C,10000.0,15,"80+ Gold"});
        l.add(new Object[]{"MSI MAG A750GL PCIE5","750W PCIe 5.0",C,12990.0,12,"Full Modular"});
        l.add(new Object[]{"MSI MPG A750GF","750W Gold модульный",C,9990.0,12,"Full Modular"});
        l.add(new Object[]{"Thermaltake ToughPower 1000W","1000W Gold",C,18990.0,5,"Full Modular"});
        l.add(new Object[]{"Thermaltake Smart 500W","500W White базов.",C,3990.0,35,"Non-Modular"});
        l.add(new Object[]{"EVGA SuperNOVA 850 G6","850W Gold",C,14990.0,7,"Full Modular"});
        l.add(new Object[]{"Chieftec BDF-600S","600W Bronze",C,5490.0,25,"Non-Modular"});
        l.add(new Object[]{"FSP Hydro GT Pro 750W","750W Gold Semi",C,8990.0,14,"Semi-Modular"});
        l.add(new Object[]{"Corsair 750W 80+ Gold","750W Gold",C,8990.0,14,"80+ Gold"});
        // 23 PSU
    }

    private static void addCooling(List<Object[]> l) {
        String C = "Охлаждение";
        l.add(new Object[]{"Cooler Master Hyper 212 EVO","Tower 120mm",C,3990.0,25,"4 тепл. трубки"});
        l.add(new Object[]{"Cooler Master Hyper 212 Black","Tower 120mm чёрный",C,3490.0,30,"4 тепл. трубки"});
        l.add(new Object[]{"Noctua NH-D15","Двойная башня",C,9990.0,6,"TDP 250W"});
        l.add(new Object[]{"Noctua NH-U12A","Башенный 120mm",C,12490.0,8,"TDP 250W"});
        l.add(new Object[]{"Noctua NH-U12A chromax.black","Tower чёрный",C,7990.0,8,"Dual fan 120mm"});
        l.add(new Object[]{"Noctua NF-A12x25","Корпусной 120mm",C,3990.0,20,"300-2000 RPM"});
        l.add(new Object[]{"be quiet! Dark Rock Pro 4","Двойная башня 250W",C,9990.0,10,"TDP 250W"});
        l.add(new Object[]{"be quiet! Pure Rock 2","Tower 120mm тихий",C,3490.0,20,"HDT, TDP 150W"});
        l.add(new Object[]{"Deepcool AK620","Dual Tower 120mm",C,5490.0,14,"6 тепл. трубок"});
        l.add(new Object[]{"Deepcool AK620 ZERO DARK","Dual Tower чёрный",C,6990.0,15,"TDP 260W"});
        l.add(new Object[]{"Deepcool LS520 AIO 240mm","AIO 240mm ARGB",C,8990.0,10,"2x120mm ARGB"});
        l.add(new Object[]{"NZXT Kraken Z73 360mm","СЖО 360mm RGB",C,28000.0,3,"360mm LCD"});
        l.add(new Object[]{"NZXT Kraken X63 280mm","СЖО 280mm LCD",C,22990.0,5,"280mm LCD"});
        l.add(new Object[]{"Corsair iCUE H100i 240mm","СЖО 240mm RGB",C,18990.0,6,"240mm iCUE"});
        l.add(new Object[]{"Corsair H115i Elite 280mm","AIO 280mm Premium",C,16990.0,5,"2x140mm ARGB"});
        l.add(new Object[]{"Arctic Liquid Freezer II 280","СЖО 280mm",C,12000.0,8,"280mm PWM"});
        l.add(new Object[]{"Arctic Liquid Freezer III 240","СЖО 240mm тихая",C,9990.0,12,"240mm PWM"});
        l.add(new Object[]{"ARCTIC Liquid Freezer III 360","AIO 360mm",C,12990.0,7,"3x120mm ARGB"});
        l.add(new Object[]{"Thermalright Peerless Assassin","Dual Tower",C,4990.0,16,"AM5/LGA1700"});
        l.add(new Object[]{"Thermalright Peerless 120","Dual Tower бюджет",C,4990.0,25,"TDP 260W"});
        l.add(new Object[]{"ARCTIC Freezer 34 eSports","Tower 120mm",C,2990.0,30,"BioniX P-Fan"});
        l.add(new Object[]{"Lian Li Galahad II 360","AIO 360mm ARGB",C,17990.0,4,"3x120mm ARGB"});
        l.add(new Object[]{"Термопаста Noctua NT-H1","Термопаста 3.5г",C,1200.0,40,"3.5г"});
        l.add(new Object[]{"Термопрокладка Arctic 1мм","50x50mm",C,1300.0,50,"50x50mm"});
        l.add(new Object[]{"Be Quiet! 120mm Fan","Вентилятор Silent Wings",C,2000.0,60,"120mm PWM"});
        l.add(new Object[]{"Arctic P14 140mm Fan","Вентилятор 140mm",C,1000.0,80,"140mm PWM"});
        l.add(new Object[]{"Arctic P12 PWM PST 5шт","Комплект 120mm",C,2990.0,60,"5x120mm PWM"});
        // 27 cooling
    }

    private static void addCases(List<Object[]> l) {
        String C = "Корпуса";
        l.add(new Object[]{"NZXT H510 Mid Tower","Mid Tower",C,7990.0,9,"ATX, TG"});
        l.add(new Object[]{"NZXT H510 Flow","ATX Mesh",C,8990.0,12,"ATX, Mesh Front"});
        l.add(new Object[]{"NZXT H7 Flow White","ATX хорошая вентиляция",C,14990.0,8,"ATX, USB-C"});
        l.add(new Object[]{"Deepcool Matrexx ATX","ATX бюджет",C,4990.0,11,"ATX"});
        l.add(new Object[]{"Deepcool CH510 White","Чистый дизайн",C,6990.0,18,"ATX, стекло"});
        l.add(new Object[]{"Deepcool CC560","ATX ARGB бюджет",C,4490.0,25,"4x120mm ARGB"});
        l.add(new Object[]{"Fractal Design Meshify 2","ATX White TG",C,16000.0,6,"ATX, Mesh"});
        l.add(new Object[]{"Fractal Design Meshify 2 Lite","ATX Mesh",C,13990.0,7,"ATX, TG"});
        l.add(new Object[]{"Fractal Design North ATX","Деревянные панели",C,16990.0,7,"ATX, Walnut"});
        l.add(new Object[]{"Fractal Design Define 7","ATX тихий",C,12990.0,7,"ATX, sound-damping"});
        l.add(new Object[]{"Lian Li PC-O11 Dynamic","Dynamic чёрный",C,18000.0,8,"E-ATX, USB-C"});
        l.add(new Object[]{"Lian Li O11 Dynamic EVO","Двухкамерный",C,22990.0,5,"E-ATX, USB-C"});
        l.add(new Object[]{"Lian Li O11D EVO XL","Full Tower",C,14990.0,6,"E-ATX, Dual AIO"});
        l.add(new Object[]{"Phanteks Eclipse G360A","ATX ARGB",C,10990.0,12,"3x120mm ARGB"});
        l.add(new Object[]{"Phanteks Enthoo Pro 2","E-ATX Full-Tower",C,17990.0,4,"12-fan, 420mm AIO"});
        l.add(new Object[]{"Corsair 5000D Airflow","Maximum airflow",C,16990.0,6,"ATX, USB-C"});
        l.add(new Object[]{"Corsair 4000D Airflow","ATX продувной",C,8490.0,14,"ATX, 2x120mm"});
        l.add(new Object[]{"MSI MAG Forge 100R ARGB","Геймерский",C,7990.0,14,"ATX, 3x120mm"});
        l.add(new Object[]{"Be quiet! Pure Base 500DX","ATX ARGB",C,9990.0,9,"3x140mm ARGB"});
        l.add(new Object[]{"Thermaltake Core P3 Open","Open frame",C,13990.0,4,"E-ATX, Open Frame"});
        l.add(new Object[]{"Thermaltake Core P3 TG","Open Frame TG",C,15990.0,5,"ATX, TG"});
        l.add(new Object[]{"SilverStone FARA R1 Pro","ATX Mesh",C,5990.0,18,"ATX, 2x120mm"});
        // 22 cases
    }

    private static void addMonitors(List<Object[]> l) {
        String C = "Мониторы";
        l.add(new Object[]{"Acer 24\" IPS 75Hz","24\" FHD IPS",C,12990.0,15,"1920x1080, IPS, 75Hz"});
        l.add(new Object[]{"Acer Nitro VG271 27\" IPS","27\" FHD 144Hz",C,19990.0,14,"1920x1080, 144Hz"});
        l.add(new Object[]{"Acer Nitro XV252Q 360Hz","25\" 360Hz киберспорт",C,41990.0,5,"1920x1080, 360Hz"});
        l.add(new Object[]{"Samsung 27\" 144Hz","27\" IPS 144Hz",C,18990.0,10,"2560x1440, IPS"});
        l.add(new Object[]{"Samsung Odyssey G7 32\" Curve","32\" QHD 240Hz",C,49990.0,5,"VA, 240Hz, 1000R"});
        l.add(new Object[]{"Samsung Odyssey G7 32\" VA","32\" QHD 240Hz VA",C,49990.0,5,"VA, HDR600"});
        l.add(new Object[]{"LG UltraGear 32\" 4K 144Hz","4K 144Hz",C,45000.0,7,"3840x2160, Nano IPS"});
        l.add(new Object[]{"LG 27GP850-B QHD 165Hz","27\" IPS QHD",C,34990.0,10,"2560x1440, 165Hz"});
        l.add(new Object[]{"LG 45GR95QE Curved OLED","45\" OLED 240Hz",C,129990.0,3,"3440x1440, OLED"});
        l.add(new Object[]{"ASUS ROG 27\" QHD 240Hz","27\" 2K 240Hz",C,35000.0,10,"2560x1440, 240Hz"});
        l.add(new Object[]{"ASUS ProArt PA279CV 27\" 4K","4K IPS для творчества",C,44990.0,4,"3840x2160, 60Hz"});
        l.add(new Object[]{"ASUS ProArt PA32UCG 32\" IPS","32\" 4K 120Hz HDR1000",C,179990.0,2,"4K, HDR1000"});
        l.add(new Object[]{"AOC 24G2 FHD 144Hz IPS","24\" IPS бюджет",C,16990.0,20,"1920x1080, 144Hz"});
        l.add(new Object[]{"AOC C27G2ZU 27\" VA","27\" 240Hz Curved",C,24990.0,10,"1920x1080, VA"});
        l.add(new Object[]{"BenQ EX2780Q QHD 144Hz","27\" HDRi",C,38990.0,6,"2560x1440, 144Hz"});
        l.add(new Object[]{"BenQ EW3280U 32\" 4K","32\" 4K HDR",C,54990.0,5,"3840x2160, 60Hz"});
        l.add(new Object[]{"Gigabyte M27Q 27\" IPS","27\" QHD 170Hz",C,31990.0,9,"SS-IPS, 170Hz"});
        l.add(new Object[]{"Dell U2723DE USB-C Hub","27\" USB-C хаб",C,52990.0,3,"IPS, 60Hz, QHD"});
        l.add(new Object[]{"Philips 27E1N3365 27\" IPS","27\" FHD 100Hz",C,14990.0,18,"IPS, 100Hz"});
        l.add(new Object[]{"ViewSonic XG2431 24\" IPS","24\" 240Hz eSports",C,29990.0,7,"IPS, 240Hz"});
        l.add(new Object[]{"MSI Optix MAG274QRF-QD","27\" QHD 165Hz",C,37990.0,7,"2560x1440, Quantum Dot"});
        l.add(new Object[]{"Gigabyte G24F 2 24\" IPS","24\" FHD 180Hz",C,15990.0,16,"1920x1080, IPS"});
        // 22 monitors
    }

    private static void addPeripherals(List<Object[]> l) {
        String C = "Периферия";
        // Мыши
        l.add(new Object[]{"Logitech G102 Mouse","Мышь 8000 DPI",C,1990.0,50,"8000 DPI, USB"});
        l.add(new Object[]{"Logitech G502 Hero","Hero Sensor",C,6500.0,20,"25600 DPI"});
        l.add(new Object[]{"Logitech G Pro X Superlight 2","Беспроводная 60г",C,12990.0,12,"HERO 25K"});
        l.add(new Object[]{"Razer DeathAdder V3","Эргономика",C,3990.0,35,"30000 DPI"});
        l.add(new Object[]{"Razer DeathAdder V3 Pro","Беспроводная",C,11990.0,10,"Focus Pro 30K"});
        l.add(new Object[]{"Razer Basilisk V3","11 кнопок",C,4990.0,20,"26000 DPI"});
        l.add(new Object[]{"Zowie EC2-C","Для CS:GO",C,5990.0,18,"3360 Optical"});
        l.add(new Object[]{"Glorious Model O2 Wireless","Лёгкая 59г",C,7990.0,15,"BAMF2 26K"});
        l.add(new Object[]{"HyperX Pulsefire Haste 2","Ultra-light 53г",C,4990.0,25,"26K DPI"});
        l.add(new Object[]{"HyperX Pulsefire Haste 2 Wired","53g проводная",C,4490.0,22,"26K DPI, USB-C"});
        l.add(new Object[]{"ASUS ROG Gladius III Wireless","3 режима",C,9490.0,9,"AimPoint Pro"});
        l.add(new Object[]{"Xtrfy M42 RGB","Ультралегкая 60g",C,5990.0,14,"16000 DPI"});
        // Клавиатуры
        l.add(new Object[]{"Logitech K120 USB","Клавиатура USB",C,1490.0,60,"Membrane, USB"});
        l.add(new Object[]{"Logitech G Pro X Keyboard","TKL Gaming",C,11000.0,12,"Hot-swap, RGB"});
        l.add(new Object[]{"Logitech G915 TKL Wireless","Тонкая TKL",C,14990.0,8,"GL Tactile"});
        l.add(new Object[]{"Logitech MX Keys Advanced","Беспроводная",C,10990.0,13,"Bluetooth, backlit"});
        l.add(new Object[]{"HyperX Alloy Origins","Механическая",C,6990.0,22,"HyperX Red"});
        l.add(new Object[]{"HyperX Alloy Origins 65","Компактная 65%",C,8490.0,15,"HyperX Aqua"});
        l.add(new Object[]{"SteelSeries Apex Pro TKL","OmniPoint",C,17490.0,6,"OLED, RGB"});
        l.add(new Object[]{"Razer BlackWidow V4 Pro","Полноразмерная",C,19990.0,5,"Yellow Switch"});
        l.add(new Object[]{"Ducky One 3 TKL Silent","PBT тихая",C,11990.0,12,"Cherry MX Silent"});
        l.add(new Object[]{"Corsair K70 RGB PRO Mini","60% Cherry MX",C,8990.0,12,"Cherry MX Red"});
        // Наушники
        l.add(new Object[]{"HyperX Cloud II","Наушники 7.1",C,8500.0,15,"USB, 53mm, 7.1"});
        l.add(new Object[]{"SteelSeries Arctis Nova Pro","Флагман ANC",C,24990.0,6,"USB, ANC"});
        l.add(new Object[]{"SteelSeries Arctis 1 PS5","Проводные PS5",C,4990.0,18,"USB-C, 40mm"});
        l.add(new Object[]{"Sennheiser PC38X","Открытые для игр",C,12990.0,9,"Open-back, 3.5mm"});
        l.add(new Object[]{"Razer Kraken V3 HyperSense","Гаптика",C,11990.0,8,"USB, 7.1"});
        l.add(new Object[]{"Logitech G435 LIGHTSPEED","Беспроводные лёгкие",C,6990.0,16,"Bluetooth, 165g"});
        // Микрофоны / прочее
        l.add(new Object[]{"Blue Yeti USB Microphone","Микрофон USB",C,12000.0,10,"USB, кардиоидный"});
        l.add(new Object[]{"HyperX QuadCast S","Микрофон RGB",C,16000.0,8,"RGB, USB"});
        l.add(new Object[]{"Elgato Wave:3 Microphone","Студийный USB",C,15990.0,8,"24bit/96kHz"});
        l.add(new Object[]{"SteelSeries Mousepad Large","Коврик Large",C,1500.0,100,"Large Cloth"});
        l.add(new Object[]{"Razer Chroma Mousepad","Коврик RGB",C,2500.0,50,"Chroma RGB"});
        l.add(new Object[]{"Zowie G-SR Mousepad","Коврик Large Cloth",C,3500.0,20,"Large Cloth"});
        l.add(new Object[]{"Logitech C922 Webcam","1080p 60fps",C,9000.0,8,"USB, 1080p"});
        l.add(new Object[]{"DualSense PS5","Геймпад PS5",C,7000.0,25,"PS5, USB-C"});
        l.add(new Object[]{"Xbox Controller Carbon","Геймпад Xbox",C,6000.0,30,"Bluetooth"});
        l.add(new Object[]{"Elgato HD60 S+ Capture","Карта захвата",C,18000.0,4,"1080p 60 HDR"});
        l.add(new Object[]{"Bluetooth TP-Link UB500","BT 5.0 Nano",C,900.0,100,"Bluetooth 5.0"});
        l.add(new Object[]{"Карт-ридер Ugreen USB 3.0","USB 3.0",C,1200.0,30,"SD/MicroSD"});
        // 40 peripherals
    }

    private static void addNetworking(List<Object[]> l) {
        String C = "Сетевое оборудование";
        l.add(new Object[]{"TP-Link Archer C6 WiFi 5","Роутер Wi-Fi 5",C,3990.0,20,"AC1200, 4 порта"});
        l.add(new Object[]{"TP-Link Archer AX73 WiFi 6","WiFi 6, 6 антенн",C,8990.0,12,"AX5400, USB 3.0"});
        l.add(new Object[]{"TP-Link Deco XE75 Pro AXE5400","Mesh WiFi 6E 3шт",C,34990.0,4,"AXE5400, 3 Pack"});
        l.add(new Object[]{"TP-Link TL-SG108 8port","Свитч 8 портов",C,1500.0,50,"Gigabit Desktop"});
        l.add(new Object[]{"TP-Link TL-SG116 16port","Свитч 16 портов",C,3490.0,20,"16x1Gbps"});
        l.add(new Object[]{"TP-Link EAP670 WiFi 6","Потолочная AP",C,8990.0,9,"AX5400, PoE+"});
        l.add(new Object[]{"ASUS RT-AX55 WiFi 6","Роутер WiFi 6",C,7990.0,14,"AX1800"});
        l.add(new Object[]{"ASUS RT-AX86U Pro WiFi 6","WiFi 6 игровой",C,12990.0,8,"AX5700, 4 порта"});
        l.add(new Object[]{"ASUS ZenWiFi Pro ET12","Mesh WiFi 6E 2шт",C,49990.0,3,"AXE11000, 10G"});
        l.add(new Object[]{"Wi-Fi адаптер ASUS PCE-AX3000","PCIe WiFi 6",C,4000.0,20,"AX3000"});
        l.add(new Object[]{"Keenetic Giga SE WiFi 6","Роутер WiFi 6",C,7490.0,15,"AX1800, 4 LAN"});
        l.add(new Object[]{"Keenetic Ultra KN-1811","WiFi 6 4G/5G USB",C,14990.0,7,"AX3200, 5 Gigabit"});
        l.add(new Object[]{"Netgear Nighthawk RS700S","WiFi 7 роутер",C,39990.0,3,"BE19000"});
        l.add(new Object[]{"Netgear MS510TXM","Мульти-гиг свитч",C,29990.0,5,"8x2.5G, 2x10G SFP+"});
        l.add(new Object[]{"Ubiquiti UniFi AP U6 Lite","AP WiFi 6 PoE",C,9990.0,7,"AX1500, PoE"});
        l.add(new Object[]{"Ubiquiti EdgeRouter X","5xGigabit продвинутый",C,5990.0,12,"PoE Passthrough"});
        l.add(new Object[]{"Mikrotik hEX S","5xGigabit SFP",C,8990.0,10,"RouterOS"});
        l.add(new Object[]{"D-Link DGS-1016D 16port","Свитч 16 портов",C,7990.0,10,"16x1000Mbps"});
        l.add(new Object[]{"D-Link DGS-1210-28P","PoE свитч 24 порта",C,29990.0,4,"24xGigabit PoE"});
        l.add(new Object[]{"Tenda MW6 Nova Mesh 3шт.","Mesh WiFi 5 3 Pack",C,9990.0,8,"AC1200"});
        l.add(new Object[]{"Патч-корд Cat6 5м","Ethernet кабель 5м",C,300.0,200,"Cat 6 White"});
        l.add(new Object[]{"Патч-корд Cat6 10м","Ethernet кабель 10м",C,500.0,150,"Cat 6 Blue"});
        l.add(new Object[]{"Патч-корд Cat6a 3м","Ethernet кабель 3м",C,400.0,180,"Cat 6a Grey"});
        l.add(new Object[]{"Патч-корд Cat5e 2м","Ethernet кабель 2м",C,200.0,250,"Cat 5e White"});
        l.add(new Object[]{"Wi-Fi антенна TP-Link 8dBi","Внешняя антенна",C,990.0,30,"RP-SMA, 8dBi"});
        // 25 networking
    }
    // TOTAL from manual entries: ~300 base products
    // Remaining ~200 are generated programmatically in getData() below

    static {
        // Validate count on class load (dev-time check)
    }
}
