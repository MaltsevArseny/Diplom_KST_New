INSERT
    OR IGNORE INTO Products (
        name,
        description,
        category,
        price,
        stock_quantity,
        specifications,
        created_at
    )
VALUES (
        'Процессор Intel Core i5-13400',
        'Процессор 10 ядер',
        'Процессоры',
        21990,
        15,
        'LGA1700, 2.5GHz',
        datetime('now')
    ),
    (
        'Процессор Intel Core i7-13700K',
        'Процессор 16 ядер',
        'Процессоры',
        38990,
        10,
        'LGA1700, 3.4GHz',
        datetime('now')
    ),
    (
        'Процессор AMD Ryzen 5 5600X',
        '6 ядер',
        'Процессоры',
        17990,
        20,
        'AM4, 3.7GHz',
        datetime('now')
    ),
    (
        'Видеокарта NVIDIA RTX 4060',
        'Видеокарта 8GB',
        'Видеокарты',
        32990,
        8,
        'GDDR6',
        datetime('now')
    ),
    (
        'Видеокарта NVIDIA RTX 4070',
        'Видеокарта 12GB',
        'Видеокарты',
        54990,
        5,
        'GDDR6X',
        datetime('now')
    ),
    (
        'Видеокарта AMD Radeon RX 7600',
        '8GB',
        'Видеокарты',
        27990,
        7,
        'GDDR6',
        datetime('now')
    ),
    (
        'Оперативная память Kingston 16GB DDR4',
        'ОЗУ',
        'Оперативная память',
        4990,
        30,
        '3200MHz',
        datetime('now')
    ),
    (
        'Оперативная память Kingston 32GB DDR4',
        'ОЗУ',
        'Оперативная память',
        8990,
        25,
        '3200MHz',
        datetime('now')
    ),
    (
        'Накопитель Samsung 970 EVO 1TB',
        'SSD',
        'Накопители',
        9990,
        18,
        'NVMe',
        datetime('now')
    ),
    (
        'Накопитель WD Blue 2TB',
        'HDD',
        'Накопители',
        5990,
        40,
        '7200rpm',
        datetime('now')
    ),
    (
        'Материнская плата ASUS Prime B760',
        'Материнская плата',
        'Материнские платы',
        15990,
        12,
        'LGA1700',
        datetime('now')
    ),
    (
        'Материнская плата MSI B550 Tomahawk',
        'Материнская плата',
        'Материнские платы',
        14990,
        10,
        'AM4',
        datetime('now')
    ),
    (
        'Блок питания Corsair 750W',
        'Блок питания',
        'Блоки питания',
        8990,
        14,
        '80+ Gold',
        datetime('now')
    ),
    (
        'Блок питания Cooler Master 650W',
        'Блок питания',
        'Блоки питания',
        6990,
        16,
        '80+ Bronze',
        datetime('now')
    ),
    (
        'Корпус NZXT H510',
        'Корпус',
        'Корпуса',
        7990,
        9,
        'Mid Tower',
        datetime('now')
    ),
    (
        'Корпус Deepcool Matrexx',
        'Корпус',
        'Корпуса',
        4990,
        11,
        'ATX',
        datetime('now')
    ),
    (
        'Мышь Logitech G102',
        'Мышь',
        'Периферия',
        1990,
        50,
        '8000 DPI',
        datetime('now')
    ),
    (
        'Мышь Razer DeathAdder',
        'Мышь',
        'Периферия',
        3990,
        35,
        '16000 DPI',
        datetime('now')
    ),
    (
        'Клавиатура Logitech K120',
        'Клавиатура',
        'Периферия',
        1490,
        60,
        'USB',
        datetime('now')
    ),
    (
        'Клавиатура HyperX Alloy',
        'Клавиатура',
        'Периферия',
        6990,
        22,
        'Mechanical',
        datetime('now')
    ),
    (
        'Монитор Acer 24"',
        'Монитор',
        'Мониторы',
        12990,
        15,
        'IPS, 75Hz',
        datetime('now')
    ),
    (
        'Монитор Samsung 27"',
        'Монитор',
        'Мониторы',
        18990,
        10,
        'IPS, 144Hz',
        datetime('now')
    ),
    (
        'Роутер TP-Link Archer C6',
        'Роутер',
        'Сетевое оборудование',
        3990,
        20,
        'Wi-Fi 5',
        datetime('now')
    ),
    (
        'Роутер ASUS RT-AX55',
        'Роутер',
        'Сетевое оборудование',
        7990,
        14,
        'Wi-Fi 6',
        datetime('now')
    ),
    (
        'Накопитель Seagate 1TB',
        'HDD',
        'Накопители',
        4990,
        25,
        '7200rpm',
        datetime('now')
    ),
    (
        'Накопитель Crucial 500GB',
        'SSD',
        'Накопители',
        4990,
        30,
        'SATA',
        datetime('now')
    ),
    (
        'Видеокарта Gigabyte RTX 4080',
        'Видеокарта',
        'Видеокарты',
        119990,
        0,
        '16GB',
        datetime('now')
    ),
    (
        'Процессор AMD Ryzen 7 5800X',
        'Процессор',
        'Процессоры',
        24990,
        12,
        '8 ядер',
        datetime('now')
    ),
    (
        'Процессор Intel Core i9-13900K',
        'Процессор',
        'Процессоры',
        59990,
        4,
        '24 ядра',
        datetime('now')
    ),
    (
        'Оперативная память G.Skill 16GB DDR5',
        'ОЗУ',
        'Оперативная память',
        7990,
        20,
        '5600MHz',
        datetime('now')
    ),
    (
        'Оперативная память G.Skill 32GB DDR5',
        'ОЗУ',
        'Оперативная память',
        14990,
        15,
        '5600MHz',
        datetime('now')
    ),
    (
        'Накопитель Samsung 980 PRO 2TB',
        'SSD',
        'Накопители',
        19990,
        8,
        'NVMe Gen4',
        datetime('now')
    ),
    (
        'Кулер Cooler Master Hyper 212',
        'Кулер',
        'Охлаждение',
        2990,
        25,
        'Воздушное',
        datetime('now')
    ),
    (
        'Кулер Noctua NH-D15',
        'Кулер',
        'Охлаждение',
        9990,
        6,
        'Воздушное',
        datetime('now')
    ),
    (
        'Коврик для мыши SteelSeries',
        'Периферия',
        'Периферия',
        1500,
        100,
        'Large',
        datetime('now')
    ),
    (
        'Наушники HyperX Cloud II',
        'Периферия',
        'Периферия',
        8500,
        15,
        '7.1 Sound',
        datetime('now')
    ),
    (
        'Микрофон Blue Yeti',
        'Периферия',
        'Периферия',
        12000,
        10,
        'USB',
        datetime('now')
    ),
    (
        'Коврик для мыши Razer',
        'Периферия',
        'Периферия',
        2500,
        50,
        'Chroma RGB',
        datetime('now')
    ),
    (
        'Клавиатура Logitech G Pro',
        'Клавиатура',
        'Периферия',
        11000,
        12,
        'TKL',
        datetime('now')
    ),
    (
        'Мышь Logitech G502',
        'Мышь',
        'Периферия',
        6500,
        20,
        'Hero Sensor',
        datetime('now')
    ),
    (
        'Веб-камера Logitech C922',
        'Периферия',
        'Периферия',
        9000,
        8,
        '1080p 60fps',
        datetime('now')
    ),
    (
        'Геймпад DualSense',
        'Периферия',
        'Периферия',
        7000,
        25,
        'PS5 White',
        datetime('now')
    ),
    (
        'Геймпад Xbox Controller',
        'Периферия',
        'Периферия',
        6000,
        30,
        'Carbon Black',
        datetime('now')
    ),
    (
        'Игровое кресло DXRacer',
        'Периферия',
        'Периферия',
        35000,
        5,
        'Premium',
        datetime('now')
    ),
    (
        'Монитор LG UltraGear 32"',
        'Монитор',
        'Мониторы',
        45000,
        7,
        '4K, 144Hz',
        datetime('now')
    ),
    (
        'Монитор ASUS ROG 27"',
        'Монитор',
        'Мониторы',
        35000,
        10,
        '2K, 240Hz',
        datetime('now')
    ),
    (
        'Карта захвата Elgato HD60 S+',
        'Периферия',
        'Периферия',
        18000,
        4,
        '1080p 60 HDR',
        datetime('now')
    ),
    (
        'SSD Samsung T7 1TB',
        'Накопители',
        'Накопители',
        12000,
        12,
        'External Blue',
        datetime('now')
    ),
    (
        'HDD WD Elements 4TB',
        'Накопители',
        'Накопители',
        10000,
        15,
        'External 3.5',
        datetime('now')
    ),
    (
        'Флешка Kingston 128GB',
        'Накопители',
        'Накопители',
        1500,
        80,
        'USB 3.2',
        datetime('now')
    ),
    (
        'Флешка Samsung 256GB',
        'Накопители',
        'Накопители',
        3500,
        40,
        'Type-C',
        datetime('now')
    ),
    (
        'Карт-ридер Ugreen',
        'Периферия',
        'Периферия',
        1200,
        30,
        'USB 3.0',
        datetime('now')
    ),
    (
        'Bluetooth адаптер TP-Link',
        'Периферия',
        'Периферия',
        900,
        100,
        '5.0 Nano',
        datetime('now')
    ),
    (
        'Wi-Fi адаптер ASUS',
        'Сетевое оборудование',
        'Сетевое оборудование',
        4000,
        20,
        'PCE-AX3000',
        datetime('now')
    ),
    (
        'Коммутатор TP-Link 8-port',
        'Сетевое оборудование',
        'Сетевое оборудование',
        1500,
        50,
        'Gigabit Desktop',
        datetime('now')
    ),
    (
        'Патч-корд 5м',
        'Сетевое оборудование',
        'Сетевое оборудование',
        300,
        200,
        'Cat 6 White',
        datetime('now')
    ),
    (
        'Патч-корд 10м',
        'Сетевое оборудование',
        'Сетевое оборудование',
        500,
        150,
        'Cat 6 Blue',
        datetime('now')
    ),
    (
        'Термопаста Noctua NT-H1',
        'Охлаждение',
        'Охлаждение',
        1200,
        40,
        '3.5г',
        datetime('now')
    ),
    (
        'Вентилятор Be Quiet! 120mm',
        'Охлаждение',
        'Охлаждение',
        2000,
        60,
        'Silent Wings 4',
        datetime('now')
    ),
    (
        'Вентилятор Arctic P14',
        'Охлаждение',
        'Охлаждение',
        1000,
        80,
        '140mm PWM',
        datetime('now')
    ),
    (
        'СЖО NZXT Kraken Z73',
        'Охлаждение',
        'Охлаждение',
        28000,
        3,
        '360mm RGB',
        datetime('now')
    ),
    (
        'СЖО Arctic Liquid Freezer II',
        'Охлаждение',
        'Охлаждение',
        12000,
        8,
        '280mm',
        datetime('now')
    ),
    (
        'Блок питания SeaSonic 1000W',
        'Блок питания',
        'Блоки питания',
        25000,
        5,
        'Prime Titanium',
        datetime('now')
    ),
    (
        'Блок питания Deepcool 850W',
        'Блок питания',
        'Блоки питания',
        10000,
        15,
        '80+ Gold Black',
        datetime('now')
    ),
    (
        'Корпус Fractal Design Meshify 2',
        'Корпус',
        'Корпуса',
        16000,
        6,
        'White TG',
        datetime('now')
    ),
    (
        'Корпус Lian Li PC-O11',
        'Корпус',
        'Корпуса',
        18000,
        8,
        'Dynamic Black',
        datetime('now')
    ),
    (
        'Процессор Intel Core i3-12100',
        'Процессор',
        'Процессоры',
        10000,
        30,
        '4 ядра LGA1700',
        datetime('now')
    ),
    (
        'Процессор AMD Ryzen 9 7950X',
        'Процессор',
        'Процессоры',
        65000,
        5,
        '16 ядер AM5',
        datetime('now')
    ),
    (
        'Материнская плата Gigabyte B650',
        'Материнская плата',
        'Материнские платы',
        18000,
        15,
        'AM5 Aorus',
        datetime('now')
    ),
    (
        'Материнская плата ASUS ROG X670E',
        'Материнская плата',
        'Материнские платы',
        45000,
        0,
        'AM5 Hero',
        datetime('now')
    ),
    (
        'Оперативная память Corsair 32GB DDR5',
        'ОЗУ',
        'Оперативная память',
        16000,
        12,
        '6000MHz RGB',
        datetime('now')
    ),
    (
        'Видеокарта MSI Suprim X RTX 4090',
        'Видеокарта',
        'Видеокарты',
        250000,
        0,
        '24GB GDDR6X',
        datetime('now')
    ),
    (
        'Видеокарта Palit Dual RTX 4060 Ti',
        'Видеокарта',
        'Видеокарты',
        45000,
        10,
        '16GB GDDR6',
        datetime('now')
    ),
    (
        'Накопитель Crucial P3 1TB',
        'SSD',
        'Накопители',
        7000,
        25,
        'PCIe 3.0',
        datetime('now')
    ),
    (
        'Накопитель Kingston KC3000 2TB',
        'SSD',
        'Накопители',
        16000,
        14,
        'PCIe 4.0',
        datetime('now')
    ),
    (
        'Накопитель WD Black SN850X 1TB',
        'SSD',
        'Накопители',
        10000,
        18,
        'Heatsink',
        datetime('now')
    ),
    (
        'Коврик для мыши Zowie G-SR',
        'Периферия',
        'Периферия',
        3500,
        20,
        'Large Cloth',
        datetime('now')
    ),
    (
        'Мышь Zowie EC2-C',
        'Мышь',
        'Периферия',
        6500,
        15,
        'Ergonomic Black',
        datetime('now')
    ),
    (
        'Клавиатура Varmilo VA87M',
        'Клавиатура',
        'Периферия',
        15000,
        3,
        'PBT Dye-Sub',
        datetime('now')
    ),
    (
        'Клавиатура Ducky One 3',
        'Клавиатура',
        'Периферия',
        13000,
        5,
        'Mini RGB Black',
        datetime('now')
    ),
    (
        'Микрофон HyperX QuadCast S',
        'Периферия',
        'Периферия',
        16000,
        8,
        'RGB White',
        datetime('now')
    ),
    (
        'Студийные мониторы PreSonus',
        'Периферия',
        'Периферия',
        14000,
        10,
        'Eris 3.5',
        datetime('now')
    ),
    (
        'Внешняя звуковая карта Focusrite',
        'Периферия',
        'Периферия',
        18000,
        5,
        'Scarlett 2i2',
        datetime('now')
    ),
    (
        'Процессор Intel Core i5-12600K',
        'Процессор',
        'Процессоры',
        25000,
        18,
        'LGA1700',
        datetime('now')
    ),
    (
        'Процессор AMD Ryzen 7 7700X',
        'Процессор',
        'Процессоры',
        35000,
        12,
        'AM5',
        datetime('now')
    ),
    (
        'Оперативная память Patriot 16GB',
        'ОЗУ',
        'Оперативная память',
        6000,
        40,
        'DDR4 3600MHz',
        datetime('now')
    ),
    (
        'Оперативная память Team Group 32GB',
        'ОЗУ',
        'Оперативная память',
        14000,
        10,
        'DDR5 6000MHz',
        datetime('now')
    ),
    (
        'Процессор Intel Core i5-11400F',
        'Процессоры',
        'Процессоры',
        12000,
        45,
        '11-е поколение',
        datetime('now')
    ),
    (
        'Процессор AMD Ryzen 3 4100',
        'Процессоры',
        'Процессоры',
        7000,
        50,
        'Бюджетный AM4',
        datetime('now')
    ),
    (
        'Видеокарта ASUS Tuf Gaming 4070 Ti',
        'Видеокарты',
        'Видеокарты',
        85000,
        6,
        '12GB OC',
        datetime('now')
    ),
    (
        'Видеокарта Gigabyte Windforce 4060',
        'Видеокарты',
        'Видеокарты',
        33000,
        15,
        'Compact Design',
        datetime('now')
    ),
    (
        'Материнская плата MSI Mag Z790 Tomahawk',
        'Материнские платы',
        'Материнские платы',
        28000,
        8,
        'LGA1700 DDR5',
        datetime('now')
    ),
    (
        'Материнская плата Gigabyte B760M DS3H',
        'Материнские платы',
        'Материнские платы',
        11000,
        20,
        'LGA1700 mATX',
        datetime('now')
    ),
    (
        'Оперативная память ADATA XPG 16GB',
        'Оперативная память',
        'Оперативная память',
        5500,
        35,
        'DDR4 3200 RGB',
        datetime('now')
    ),
    (
        'Оперативная память Silicon Power 16GB',
        'Оперативная память',
        'Оперативная память',
        4000,
        60,
        'DDR4 3200',
        datetime('now')
    ),
    (
        'SSD Kingston NV2 500GB',
        'Накопители',
        'Накопители',
        4000,
        40,
        'PCIe 4.0 NVMe',
        datetime('now')
    ),
    (
        'SSD ADATA Legend 960 1TB',
        'Накопители',
        'Накопители',
        8500,
        18,
        'PCIe 4.0 7400/6000 MB/s',
        datetime('now')
    ),
    (
        'HDD WD Purple 4TB',
        'Накопители',
        'Накопители',
        11000,
        12,
        'Для видеонаблюдения',
        datetime('now')
    ),
    (
        'HDD Seagate IronWolf 2TB',
        'Накопители',
        'Накопители',
        9000,
        15,
        'Для NAS',
        datetime('now')
    ),
    (
        'Блок питания FSP Hydro G Pro 750W',
        'Блоки питания',
        'Блоки питания',
        11500,
        10,
        '80+ Gold Full Modular',
        datetime('now')
    ),
    (
        'Блок питания Aerocool Cylon 600W',
        'Блоки питания',
        'Блоки питания',
        4500,
        25,
        '80+ RGB',
        datetime('now')
    ),
    (
        'Корпус Cougar MX330-G',
        'Корпуса',
        'Корпуса',
        5500,
        18,
        'Mid Tower Mesh',
        datetime('now')
    ),
    (
        'Корпус Zalman i3 Edge',
        'Корпуса',
        'Корпуса',
        6000,
        12,
        '4x Blue LED Fans',
        datetime('now')
    ),
    (
        'Кулер ID-Cooling SE-224-XTS',
        'Охлаждение',
        'Охлаждение',
        2200,
        45,
        '180W Black',
        datetime('now')
    ),
    (
        'СЖО Deepcool LS720',
        'Охлаждение',
        'Охлаждение',
        11000,
        7,
        '360mm RGB White',
        datetime('now')
    ),
    (
        'Монитор Xiaomi Mi 23.8',
        'Мониторы',
        'Мониторы',
        10000,
        30,
        'IPS FHD 75Hz',
        datetime('now')
    ),
    (
        'Монитор GIGABYTE M27Q',
        'Мониторы',
        'Мониторы',
        32000,
        10,
        'SS IPS 2K 170Hz',
        datetime('now')
    ),
    (
        'Мышь Glorious Model O',
        'Периферия',
        'Периферия',
        5500,
        20,
        'Lightweight Matte White',
        datetime('now')
    ),
    (
        'Клавиатура Keychron K2',
        'Периферия',
        'Периферия',
        9500,
        8,
        'Wireless Mechanical RGB',
        datetime('now')
    ),
    (
        'Наушники Sennheiser HD 560S',
        'Периферия',
        'Периферия',
        19000,
        5,
        'Open Back Studio',
        datetime('now')
    ),
    (
        'Колонки Edifier R1280DB',
        'Периферия',
        'Периферия',
        12000,
        15,
        'Bluetooth Active Speakers',
        datetime('now')
    ),
    (
        'SSD Netac NV7000 2TB',
        'Накопители',
        'Накопители',
        14000,
        10,
        'PS5 Compatible',
        datetime('now')
    ),
    (
        'Процессор Intel Core i7-12700F',
        'Процессоры',
        'Процессоры',
        31000,
        12,
        'LGA1700 12 ядер',
        datetime('now')
    ),
    (
        'Видеокарта INNO3D RTX 3060',
        'Видеокарты',
        'Видеокарты',
        31000,
        18,
        '12GB Twin X2 LHR',
        datetime('now')
    ),
    (
        'Материнская плата ASRock B660 Steel Legend',
        'Материнские платы',
        'Материнские платы',
        16000,
        10,
        'LGA1700 DDR4',
        datetime('now')
    ),
    (
        'Блок питания be quiet! System Power 10 650W',
        'Блоки питания',
        'Блоки питания',
        7500,
        15,
        '80+ Bronze',
        datetime('now')
    ),
    (
        'Процессор Intel Core i5-11400',
        'Процессор',
        'Процессоры',
        12500,
        30,
        '6 ядер 12 потоков',
        datetime('now')
    ),
    (
        'Видеокарта PowerColor Radeon RX 6600',
        'Видеокарта',
        'Видеокарты',
        25000,
        12,
        '8GB Fighter',
        datetime('now')
    ),
    (
        'Материнская плата ASUS TUF B550-PLUS',
        'Материнская плата',
        'Материнские платы',
        14500,
        14,
        'AM4 Wi-Fi',
        datetime('now')
    ),
    (
        'Оперативная память Crucial 16GB DDR4',
        'ОЗУ',
        'Оперативная память',
        4200,
        50,
        '2x8GB 3200MHz',
        datetime('now')
    ),
    (
        'SSD Samsung 970 EVO Plus 500GB',
        'SSD',
        'Накопители',
        6500,
        20,
        'NVMe Gen3',
        datetime('now')
    ),
    (
        'HDD WD Red Plus 4TB',
        'HDD',
        'Накопители',
        11500,
        10,
        'NAS Drive',
        datetime('now')
    ),
    (
        'Блок питания Aerocool KCAS 700W',
        'Блок питания',
        'Блоки питания',
        4800,
        15,
        '80+ Bronze RGB',
        datetime('now')
    ),
    (
        'Корпус Zalman S3',
        'Корпус',
        'Корпуса',
        4500,
        12,
        'Acrylic Side Panel',
        datetime('now')
    ),
    (
        'Кулер Arctic Freezer i35',
        'Кулер',
        'Охлаждение',
        3500,
        18,
        'LGA1700 ARGB',
        datetime('now')
    ),
    (
        'Монитор AOC 24G2U',
        'Монитор',
        'Мониторы',
        16500,
        10,
        'IPS 144Hz',
        datetime('now')
    ),
    (
        'Клавиатура Razer BlackWidow V3',
        'Клавиатура',
        'Периферия',
        12000,
        8,
        'Green Switches',
        datetime('now')
    ),
    (
        'Мышь SteelSeries Rival 3',
        'Мышь',
        'Периферия',
        3200,
        25,
        'Wireless',
        datetime('now')
    ),
    (
        'Наушники Razer Kraken X',
        'Наушники',
        'Периферия',
        4500,
        15,
        '7.1 Surround',
        datetime('now')
    ),
    (
        'Коврик для мыши HyperX Fury S',
        'Коврик',
        'Периферия',
        1800,
        40,
        'Extra Large',
        datetime('now')
    ),
    (
        'Wi-Fi роутер Tenda AC10',
        'Сетевое оборудование',
        'Сетевое оборудование',
        2800,
        20,
        'Dual Band Gigabit',
        datetime('now')
    ),
    (
        'Процессор Intel Core i5-12400F OEM',
        'Процессоры',
        'Процессоры',
        14500,
        40,
        'Poplar Seller LGA1700',
        datetime('now')
    ),
    (
        'Видеокарта Gigabyte RTX 4060 Eagle OC',
        'Видеокарты',
        'Видеокарты',
        34000,
        12,
        '8GB G6X',
        datetime('now')
    ),
    (
        'Материнская плата MSI PRO B660M-A',
        'Материнские платы',
        'Материнские платы',
        12000,
        18,
        'Best seller mATX',
        datetime('now')
    ),
    (
        'Оперативная память G.Skill RIPJAWS V 16GB',
        'Оперативная память',
        'Оперативная память',
        4800,
        30,
        '3600MHz CL18',
        datetime('now')
    ),
    (
        'SSD ADATA SX8200 Pro 1TB',
        'Накопители',
        'Накопители',
        7500,
        22,
        'Best value Gen3',
        datetime('now')
    ),
    (
        'HDD WD Blue 1TB',
        'Накопители',
        'Накопители',
        4500,
        45,
        'Basic 7200rpm',
        datetime('now')
    ),
    (
        'Блок питания Cooler Master MWE 600',
        'Блоки питания',
        'Блоки питания',
        6500,
        20,
        'Bronze V2 White',
        datetime('now')
    ),
    (
        'Корпус Deepcool CC560',
        'Корпуса',
        'Корпуса',
        5800,
        14,
        '4x LED Fans Black',
        datetime('now')
    ),
    (
        'Кулер ID-Cooling SE-214-XT',
        'Охлаждение',
        'Охлаждение',
        1600,
        50,
        'ARGB PWM Black',
        datetime('now')
    ),
    (
        'Монитор MSI Optix G241',
        'Мониторы',
        'Мониторы',
        17000,
        10,
        'IPS 144Hz Esports',
        datetime('now')
    ),
    (
        'Мышь Bloody V7',
        'Периферия',
        'Периферия',
        1800,
        60,
        'Classic Holeless',
        datetime('now')
    ),
    (
        'Клавиатура Dark Project KD87A',
        'Периферия',
        'Периферия',
        9000,
        10,
        'Optical Gateron Teal',
        datetime('now')
    ),
    (
        'Наушники JBL Quantum 100',
        'Периферия',
        'Периферия',
        3200,
        20,
        'Wired Blue',
        datetime('now')
    ),
    (
        'ПК-вентилятор ID-Cooling TF-12025',
        'Охлаждение',
        'Охлаждение',
        700,
        100,
        '120mm Black',
        datetime('now')
    ),
    (
        'Разветвитель питания вентиляторов',
        'Периферия',
        'Периферия',
        400,
        100,
        '1 to 10 Hub',
        datetime('now')
    ),
    (
        'Термопаста GD900',
        'Охлаждение',
        'Охлаждение',
        300,
        500,
        '30г шприц',
        datetime('now')
    ),
    (
        'Процессор Intel Core i7-11700',
        'Процессоры',
        'Процессоры',
        23000,
        15,
        '8 ядер 16 потоков',
        datetime('now')
    ),
    (
        'Видеокарта ASUS Dual RX 6600 V2',
        'Видеокарты',
        'Видеокарты',
        26000,
        10,
        '8GB GDDR6',
        datetime('now')
    ),
    (
        'Материнская плата Gigabyte H610M S2',
        'Материнские платы',
        'Материнские платы',
        8500,
        25,
        'LGA1700 mATX Basic',
        datetime('now')
    ),
    (
        'Процессор Intel Celeron G6900',
        'Процессоры',
        'Процессоры',
        4500,
        20,
        'Dual Core Office',
        datetime('now')
    ),
    (
        'Процессор Intel Core i3-10100F',
        'Процессоры',
        'Процессоры',
        6500,
        40,
        'Cheap Gaming',
        datetime('now')
    ),
    (
        'Процессор AMD Ryzen 5 4500',
        'Процессоры',
        'Процессоры',
        7500,
        30,
        'Budget 6-core',
        datetime('now')
    ),
    (
        'Процессор AMD Ryzen 5 5500',
        'Процессоры',
        'Процессоры',
        9500,
        50,
        'Best budget king',
        datetime('now')
    ),
    (
        'Процессор AMD Athlon 3000G',
        'Процессоры',
        'Процессоры',
        5000,
        15,
        'Vega 3 Graphics',
        datetime('now')
    ),
    (
        'Видеокарта MSI Ventus 2X 4060',
        'Видеокарты',
        'Видеокарты',
        33500,
        12,
        'Dual Fan OC',
        datetime('now')
    ),
    (
        'Видеокарта Palit JetStream 4070 Ti',
        'Видеокарты',
        'Видеокарты',
        84000,
        5,
        'Premium Cooling',
        datetime('now')
    ),
    (
        'Видеокарта ASUS Phoenix 1650',
        'Видеокарты',
        'Видеокарты',
        15000,
        8,
        'No power connector',
        datetime('now')
    ),
    (
        'Видеокарта KFA2 RTX 3050',
        'Видеокарты',
        'Видеокарты',
        22000,
        10,
        '8GB Core',
        datetime('now')
    ),
    (
        'Материнская плата MSI Pro H610M-G',
        'Материнские платы',
        'Материнские платы',
        8800,
        30,
        'Office standard',
        datetime('now')
    ),
    (
        'Материнская плата ASUS B650 Gaming Wi-Fi',
        'Материнские платы',
        'Материнские платы',
        21000,
        8,
        'AM5 ROG Strix',
        datetime('now')
    ),
    (
        'Материнская плата ASRock A320M-HDV',
        'Материнские платы',
        'Материнские платы',
        5500,
        20,
        'Cheap AM4',
        datetime('now')
    ),
    (
        'Материнская плата Gigabyte Z790 UD',
        'Материнские платы',
        'Материнские платы',
        22000,
        12,
        'Intel 13th build',
        datetime('now')
    ),
    (
        'Оперативная память Crucial 8GB',
        'Оперативная память',
        'Оперативная память',
        1800,
        100,
        'DDR4 2666',
        datetime('now')
    ),
    (
        'Оперативная память Samsung 8GB',
        'Оперативная память',
        'Оперативная память',
        2200,
        80,
        'DDR4 3200',
        datetime('now')
    ),
    (
        'Оперативная память Kingston FURY 64GB',
        'Оперативная память',
        'Оперативная память',
        28000,
        5,
        'DDR5 6000 2x32',
        datetime('now')
    ),
    (
        'Оперативная память ADATA 8GB',
        'Оперативная память',
        'Оперативная память',
        2000,
        150,
        'Budget stick',
        datetime('now')
    ),
    (
        'SSD SATA Apacer 120GB',
        'Накопители',
        'Накопители',
        1500,
        40,
        'OS drive cheap',
        datetime('now')
    ),
    (
        'SSD NVMe Kingston A400 240GB',
        'Накопители',
        'Накопители',
        2500,
        35,
        'SATA legacy',
        datetime('now')
    ),
    (
        'SSD NVMe WD Black 2TB',
        'Накопители',
        'Накопители',
        21000,
        5,
        'Performance Gen4',
        datetime('now')
    ),
    (
        'SSD NVMe ADATA XPG 2TB',
        'Накопители',
        'Накопители',
        16000,
        8,
        'Gaming drive RGB',
        datetime('now')
    ),
    (
        'HDD Toshiba P300 1TB',
        'Накопители',
        'Накопители',
        4200,
        30,
        'Standard 7200',
        datetime('now')
    ),
    (
        'HDD Seagate Barracuda 4TB',
        'Накопители',
        'Накопители',
        10500,
        10,
        'Mass storage',
        datetime('now')
    ),
    (
        'HDD WD Blue 500GB',
        'Накопители',
        'Накопители',
        2800,
        25,
        'Renewed/Refurb',
        datetime('now')
    ),
    (
        'HDD Toshiba NAS 8TB',
        'Накопители',
        'Накопители',
        22000,
        4,
        'Server grade',
        datetime('now')
    ),
    (
        'Блок питания PowerMan 450W',
        'Блоки питания',
        'Блоки питания',
        2500,
        20,
        'Office only',
        datetime('now')
    ),
    (
        'Блок питания Cooler Master 400W',
        'Блоки питания',
        'Блоки питания',
        3500,
        15,
        'Elite V3',
        datetime('now')
    ),
    (
        'Блок питания Chieftec 600W',
        'Блоки питания',
        'Блоки питания',
        5500,
        25,
        'Element Series',
        datetime('now')
    ),
    (
        'Блок питания Seasonic 650W',
        'Блоки питания',
        'Блоки питания',
        12000,
        10,
        'Core Gold',
        datetime('now')
    ),
    (
        'Корпус Ginzzu CL180',
        'Корпуса',
        'Корпуса',
        3000,
        15,
        'Office White',
        datetime('now')
    ),
    (
        'Корпус Powercase Mistral',
        'Корпуса',
        'Корпуса',
        4500,
        20,
        'ARGB Mesh',
        datetime('now')
    ),
    (
        'Корпус 1STPLAYER FireRose',
        'Корпуса',
        'Корпуса',
        3800,
        12,
        'T6 Black',
        datetime('now')
    ),
    (
        'Корпус AeroCool Aero One',
        'Корпуса',
        'Корпуса',
        6500,
        8,
        'Mini Frost',
        datetime('now')
    ),
    (
        'Кулер AeroCool Air Frost 2',
        'Охлаждение',
        'Охлаждение',
        900,
        100,
        'RGB Top Flow',
        datetime('now')
    ),
    (
        'Кулер Deepcool Gamma Archer',
        'Охлаждение',
        'Охлаждение',
        1100,
        80,
        'Basic Alum',
        datetime('now')
    ),
    (
        'Кулер Be Quiet! Pure Rock 2',
        'Охлаждение',
        'Охлаждение',
        5500,
        15,
        'Silver Black',
        datetime('now')
    ),
    (
        'Кулер PCCOOLER GI-X4',
        'Охлаждение',
        'Охлаждение',
        1800,
        30,
        'Budget Tower ARGB',
        datetime('now')
    ),
    (
        'Периферия Sven GC-W400',
        'Периферия',
        'Периферия',
        CommWheel,
        10,
        'Игровой руль',
        datetime('now')
    ),
    (
        'Периферия Redragon Kumara',
        'Периферия',
        'Периферия',
        4500,
        20,
        'Mechanical Blue',
        datetime('now')
    ),
    (
        'Периферия A4Tech X7',
        'Периферия',
        'Периферия',
        1500,
        45,
        'Mouse Pad XL',
        datetime('now')
    ),
    (
        'Периферия Defender',
        'Периферия',
        'Периферия',
        500,
        200,
        'Mouse Desk Mat',
        datetime('now')
    ),
    (
        'Монитор Philips 24"',
        'Мониторы',
        'Мониторы',
        9500,
        20,
        'Simple Office',
        datetime('now')
    ),
    (
        'Монитор BenQ 24"',
        'Мониторы',
        'Мониторы',
        13000,
        12,
        'Zowie Gaming',
        datetime('now')
    ),
    (
        'Монитор AOC 27"',
        'Мониторы',
        'Мониторы',
        21000,
        8,
        'Curved 165Hz',
        datetime('now')
    ),
    (
        'Монитор Dell 24"',
        'Мониторы',
        'Мониторы',
        15000,
        15,
        'Ultrasharp IPS',
        datetime('now')
    ),
    (
        'Сетевое оборудование Keenetic Runner',
        'Сетевое оборудование',
        'Сетевое оборудование',
        8500,
        15,
        'LTE Router',
        datetime('now')
    ),
    (
        'Сетевое оборудование TP-Link Nano',
        'Сетевое оборудование',
        'Сетевое оборудование',
        1200,
        50,
        'USB Adapter',
        datetime('now')
    ),
    (
        'Сетевое оборудование Netgear 5-port',
        'Сетевое оборудование',
        'Сетевое оборудование',
        2500,
        10,
        'Unmanaged Switch',
        datetime('now')
    ),
    (
        'Процессор Intel Core i7-10700K',
        'Процессоры',
        'Процессоры',
        21000,
        10,
        'LGA1200 OC',
        datetime('now')
    ),
    (
        'Видеокарта Gigabyte GTX 1650',
        'Видеокарты',
        'Видеокарты',
        16500,
        15,
        'OC Low Profile',
        datetime('now')
    ),
    (
        'Материнская плата MSI B450-A PRO',
        'Материнские платы',
        'Материнские платы',
        9500,
        12,
        'AM4 ATX Classic',
        datetime('now')
    ),
    (
        'Блок питания FSP PNR 500W',
        'Блоки питания',
        'Блоки питания',
        3500,
        30,
        'OEM Power',
        datetime('now')
    ),
    (
        'Оперативная память Crucial 4GB',
        'Оперативная память',
        'Оперативная память',
        1200,
        100,
        'DDR4 2400',
        datetime('now')
    ),
    (
        'SSD SATA Kingston A400 120GB',
        'Накопители',
        'Накопители',
        1600,
        60,
        'OS Cheap',
        datetime('now')
    ),
    (
        'HDD WD Blue 250GB',
        'Накопители',
        'Накопители',
        1500,
        20,
        'Recycled',
        datetime('now')
    ),
    (
        'Корпус ExeGate BAA-100',
        'Корпуса',
        'Корпуса',
        2200,
        25,
        'Small Office',
        datetime('now')
    ),
    (
        'Кулер Intel Stock LGA1200',
        'Охлаждение',
        'Охлаждение',
        500,
        100,
        'Original',
        datetime('now')
    ),
    (
        'Клавиатура OKLICK 100M',
        'Периферия',
        'Периферия',
        600,
        100,
        'Office Basic',
        datetime('now')
    ),
    (
        'Мышь Defender Dacota',
        'Периферия',
        'Периферия',
        400,
        150,
        'Classic Optical',
        datetime('now')
    ),
    (
        'Вентилятор 80mm generic',
        'Охлаждение',
        'Охлаждение',
        200,
        200,
        'Basic Case Fan',
        datetime('now')
    ),
    (
        'Процессор AMD Ryzen 5 3600',
        'Процессоры',
        'Процессоры',
        11000,
        25,
        'Zen 2 classic',
        datetime('now')
    ),
    (
        'Видеокарта Palit GTX 1050 Ti',
        'Видеокарты',
        'Видеокарты',
        10000,
        5,
        'StormX 4GB',
        datetime('now')
    ),
    (
        'Материнская плата ASUS H81M-K',
        'Материнские платы',
        'Материнские платы',
        5000,
        3,
        'LGA1150 Legacy',
        datetime('now')
    ),
    (
        'Блок питания Zalman Wattbit 500W',
        'Блоки питания',
        'Блоки питания',
        3800,
        15,
        '83+ Efficiency',
        datetime('now')
    ),
    (
        'Оперативная память Kingmax 8GB',
        'Оперативная память',
        'Оперативная память',
        2100,
        20,
        'DDR3 1600MHz',
        datetime('now')
    ),
    (
        'SSD SATA Goldenfir 240GB',
        'Накопители',
        'Накопители',
        1800,
        30,
        'Budget China',
        datetime('now')
    ),
    (
        'HDD WD Green 1TB',
        'Накопители',
        'Накопители',
        3500,
        10,
        'Eco Drive Storage',
        datetime('now')
    ),
    (
        'Корпус Ginzzu D350',
        'Корпуса',
        'Корпуса',
        2800,
        14,
        'Black mATX',
        datetime('now')
    ),
    (
        'Кулер AMD Wraith Stealth',
        'Охлаждение',
        'Охлаждение',
        700,
        50,
        'Original AM4',
        datetime('now')
    ),
    (
        'Клавиатура Genius KB-110',
        'Периферия',
        'Периферия',
        800,
        100,
        'Classic PS/2',
        datetime('now')
    ),
    (
        'Мышь A4Tech OP-620D',
        'Периферия',
        'Периферия',
        600,
        120,
        'Standard Optical',
        datetime('now')
    ),
    (
        'Переходник DVI-VGA',
        'Периферия',
        'Периферия',
        300,
        300,
        'Active Converter',
        datetime('now')
    ),
    (
        'Кабель HDMI 1.5м',
        'Периферия',
        'Периферия',
        400,
        500,
        'v2.0 4K Black',
        datetime('now')
    ),
    (
        'Динамик-пищалка для ПК',
        'Периферия',
        'Периферия',
        100,
        1000,
        'Motherboard Buzzer',
        datetime('now')
    ),
    (
        'Термопаста КПТ-8',
        'Охлаждение',
        'Охлаждение',
        150,
        2000,
        '10г Тюбик',
        datetime('now')
    ),
    (
        'Процессор Intel Core i3-12100F Box',
        'Процессоры',
        'Процессоры',
        11500,
        25,
        'With Stock Cooler',
        datetime('now')
    ),
    (
        'Видеокарта GIGABYTE RTX 3050 Windforce',
        'Видеокарты',
        'Видеокарты',
        23000,
        10,
        '8GB GDDR6 Black',
        datetime('now')
    ),
    (
        'Материнская плата MSI PRO H610M-E',
        'Материнские платы',
        'Материнские платы',
        8200,
        20,
        'LGA1700 DDR4 mATX',
        datetime('now')
    ),
    (
        'Оперативная память Patriot Viper 16GB',
        'Оперативная память',
        'Оперативная память',
        5200,
        40,
        '3200MHz Red',
        datetime('now')
    ),
    (
        'SSD M.2 Samsung 980 500GB',
        'Накопители',
        'Накопители',
        6000,
        15,
        'PCIe 3.0 NVMe',
        datetime('now')
    ),
    (
        'HDD WD Blue 2TB 7200',
        'Накопители',
        'Накопители',
        6500,
        12,
        'Standard Storage',
        datetime('now')
    ),
    (
        'Блок питания DeepCool PK600D',
        'Блоки питания',
        'Блоки питания',
        5500,
        14,
        '80+ Bronze Certified',
        datetime('now')
    ),
    (
        'Корпус AeroCool Cylon Mini',
        'Корпуса',
        'Корпуса',
        4200,
        10,
        'RGB mATX Black',
        datetime('now')
    ),
    (
        'Кулер ID-Cooling DK-03',
        'Охлаждение',
        'Охлаждение',
        1000,
        80,
        'RGB Top Flow Basic',
        datetime('now')
    ),
    (
        'Монитор Samsung Odyssey G3',
        'Мониторы',
        'Мониторы',
        19000,
        8,
        '24" 144Hz VA',
        datetime('now')
    ),
    (
        'Мышь Logitech G305',
        'Периферия',
        'Периферия',
        4500,
        20,
        'Wireless Lightspeed',
        datetime('now')
    ),
    (
        'Клавиатура Redragon Fizz',
        'Периферия',
        'Периферия',
        3500,
        15,
        '60% Mechanical RGB',
        datetime('now')
    ),
    (
        'Наушники Razer BlackShark V2 X',
        'Периферия',
        'Периферия',
        5500,
        10,
        'Gaming 7.1',
        datetime('now')
    ),
    (
        'Веб-камера A4Tech PK-910H',
        'Периферия',
        'Периферия',
        2500,
        25,
        '1080p FHD',
        datetime('now')
    ),
    (
        'Вентилятор DEEPCOOL FK120',
        'Охлаждение',
        'Охлаждение',
        1100,
        50,
        'Performance PWM',
        datetime('now')
    ),
    (
        'Кабель DisplayPort 2м',
        'Периферия',
        'Периферия',
        800,
        100,
        '4K 144Hz Certified',
        datetime('now')
    ),
    (
        'Термопрокладка Arctic 1мм',
        'Охлаждение',
        'Охлаждение',
        1300,
        50,
        '50x50mm',
        datetime('now')
    );
'14-е поколение, 14 ядер (6P+8E)',
'Процессоры',
32990,
14,
'LGA1700, 3.5GHz, TDP 125W',
datetime('now')
),
(
    'Intel Core i7-14700K',
    '14-е поколение, 20 ядер',
    'Процессоры',
    47990,
    8,
    'LGA1700, 3.4GHz, TDP 125W',
    datetime('now')
),
(
    'Intel Core i9-14900K',
    '14-е поколение, 24 ядра',
    'Процессоры',
    69990,
    4,
    'LGA1700, 3.2GHz, TDP 125W',
    datetime('now')
),
(
    'Intel Core i3-13100F',
    '4 ядра без встроенной графики',
    'Процессоры',
    9990,
    30,
    'LGA1700, 3.4GHz, TDP 58W',
    datetime('now')
),
(
    'Intel Core i5-13600KF',
    '14 ядер, без встроенной графики',
    'Процессоры',
    28990,
    18,
    'LGA1700, 3.5GHz, TDP 125W',
    datetime('now')
),
(
    'AMD Ryzen 5 7600',
    '6 ядер, AM5 платформа',
    'Процессоры',
    21990,
    20,
    'AM5, 3.8GHz, TDP 65W',
    datetime('now')
),
(
    'AMD Ryzen 7 7700',
    '8 ядер, AM5 платформа',
    'Процессоры',
    31990,
    12,
    'AM5, 3.8GHz, TDP 65W',
    datetime('now')
),
(
    'AMD Ryzen 9 7900X',
    '12 ядер, AM5 High-End',
    'Процессоры',
    52990,
    6,
    'AM5, 4.7GHz, TDP 170W',
    datetime('now')
),
(
    'AMD Ryzen 5 5500',
    '6 ядер AM4, бюджет',
    'Процессоры',
    9490,
    40,
    'AM4, 3.6GHz, TDP 65W',
    datetime('now')
),
(
    'AMD Ryzen 9 5900X',
    '12 ядер AM4, флагман',
    'Процессоры',
    29990,
    5,
    'AM4, 3.7GHz, TDP 105W',
    datetime('now')
),
(
    'Intel Pentium Gold G7400',
    '2 ядра, офис бюджет',
    'Процессоры',
    5990,
    50,
    'LGA1700, 3.7GHz, TDP 46W',
    datetime('now')
),
(
    'Intel Core i5-13400F',
    '10 ядер без встр. графики',
    'Процессоры',
    19990,
    22,
    'LGA1700, 2.5GHz, TDP 65W',
    datetime('now')
),
(
    'AMD Ryzen 3 4100',
    '4 ядра AM4, бюджет',
    'Процессоры',
    6990,
    45,
    'AM4, 3.8GHz, TDP 65W',
    datetime('now')
),
(
    'AMD Ryzen 7 5700X',
    '8 ядер AM4',
    'Процессоры',
    19990,
    16,
    'AM4, 3.4GHz, TDP 65W',
    datetime('now')
),
(
    'Intel Core i9-13900KS',
    'Флагман, 6.0GHz Boost',
    'Процессоры',
    79990,
    3,
    'LGA1700, 3.2GHz, TDP 150W',
    datetime('now')
),
(
    'NVIDIA RTX 4090 Founders Edition',
    'Топовая карта 24GB GDDR6X',
    'Видеокарты',
    249990,
    2,
    '24GB GDDR6X, 450W TDP',
    datetime('now')
),
(
    'NVIDIA RTX 4070 Ti Super',
    '16GB, отличная 2K/4K производительность',
    'Видеокарты',
    79990,
    5,
    '16GB GDDR6X, 285W',
    datetime('now')
),
(
    'NVIDIA RTX 4070 Super',
    '12GB, лучшее соотношение ц/к',
    'Видеокарты',
    59990,
    8,
    '12GB GDDR6X, 220W',
    datetime('now')
),
(
    'NVIDIA RTX 4060 Ti 16GB',
    '16GB, увеличенный видеобуфер',
    'Видеокарты',
    49990,
    10,
    '16GB GDDR6, 165W',
    datetime('now')
),
(
    'NVIDIA RTX 3060 12GB',
    '12GB, популярный выбор',
    'Видеокарты',
    29990,
    15,
    '12GB GDDR6, 170W',
    datetime('now')
),
(
    'NVIDIA RTX 3080 10GB',
    '10GB, 4K игровая',
    'Видеокарты',
    54990,
    4,
    '10GB GDDR6X, 320W',
    datetime('now')
),
(
    'AMD Radeon RX 7900 XTX',
    'Флагман AMD, 24GB',
    'Видеокарты',
    89990,
    3,
    '24GB GDDR6, 355W',
    datetime('now')
),
(
    'AMD Radeon RX 7800 XT',
    '16GB, отличный 1440p',
    'Видеокарты',
    49990,
    7,
    '16GB GDDR6, 263W',
    datetime('now')
),
(
    'AMD Radeon RX 7600 XT',
    '16GB, народный выбор',
    'Видеокарты',
    34990,
    14,
    '16GB GDDR6, 165W',
    datetime('now')
),
(
    'AMD Radeon RX 6600 XT',
    '8GB, 1080p максимум',
    'Видеокарты',
    24990,
    18,
    '8GB GDDR6, 160W',
    datetime('now')
),
(
    'NVIDIA GTX 1660 Super',
    '6GB, бюджетный 1080p',
    'Видеокарты',
    16990,
    20,
    '6GB GDDR6, 125W',
    datetime('now')
),
(
    'AMD Radeon RX 6500 XT',
    '4GB, офис/эконом',
    'Видеокарты',
    12990,
    25,
    '4GB GDDR6, 107W',
    datetime('now')
),
(
    'NVIDIA RTX 4080 Super 16GB',
    '16GB, 4K Ultra',
    'Видеокарты',
    109990,
    4,
    '16GB GDDR6X, 320W',
    datetime('now')
),
(
    'Palit RTX 4060 Dual 8GB',
    '8GB, тихая и компактная',
    'Видеокарты',
    37990,
    16,
    '8GB GDDR6, 115W',
    datetime('now')
),
(
    'ASUS TUF Gaming RX 7700 XT',
    '12GB, геймерская серия',
    'Видеокарты',
    41990,
    9,
    '12GB GDDR6, 245W',
    datetime('now')
),
(
    'Kingston Fury Beast 8GB DDR4 3200',
    'Один модуль 8GB',
    'Оперативная память',
    2490,
    60,
    'DDR4, 3200MHz, CL16',
    datetime('now')
),
(
    'Kingston Fury Beast 16GB DDR4 3600',
    'Один модуль 16GB',
    'Оперативная память',
    4490,
    40,
    'DDR4, 3600MHz, CL18',
    datetime('now')
),
(
    'Corsair Vengeance RGB 32GB DDR4',
    '2x16GB с RGB подсветкой',
    'Оперативная память',
    9990,
    20,
    'DDR4, 3600MHz, CL18, RGB',
    datetime('now')
),
(
    'G.Skill Trident Z5 32GB DDR5',
    '2x16GB, DDR5 топ сегмент',
    'Оперативная память',
    17990,
    10,
    'DDR5, 6000MHz, CL36',
    datetime('now')
),
(
    'Crucial 8GB DDR4 3200',
    'Бюджетный вариант 8GB',
    'Оперативная память',
    1990,
    80,
    'DDR4, 3200MHz, CL22',
    datetime('now')
),
(
    'Crucial 16GB DDR5 4800',
    'Офисный DDR5 модуль',
    'Оперативная память',
    6490,
    30,
    'DDR5, 4800MHz, CL40',
    datetime('now')
),
(
    'Samsung 8GB DDR4 2666 SO-DIMM',
    'OEM для ноутбуков',
    'Оперативная память',
    2290,
    50,
    'SO-DIMM, DDR4, 2666MHz',
    datetime('now')
),
(
    'HyperX Impact 32GB DDR4 SO-DIMM',
    '2x16GB для ноутбука',
    'Оперативная память',
    8990,
    15,
    'SO-DIMM, DDR4, 3200MHz',
    datetime('now')
),
(
    'Team T-Force Vulcan 16GB DDR4',
    'Геймерская с RGB',
    'Оперативная память',
    3990,
    35,
    'DDR4, 3200MHz, CL16, RGB',
    datetime('now')
),
(
    'ADATA XPG Spectrix 64GB DDR5',
    '2x32GB, топовый комплект',
    'Оперативная память',
    29990,
    5,
    'DDR5, 5600MHz, CL36',
    datetime('now')
),
(
    'Patriot Viper 4 16GB DDR4',
    'Надёжная, без RGB',
    'Оперативная память',
    3490,
    45,
    'DDR4, 3200MHz, CL16',
    datetime('now')
),
(
    'Kingston Fury Renegade 32GB DDR5',
    '2x16GB скоростная DDR5',
    'Оперативная память',
    19990,
    8,
    'DDR5, 6400MHz, CL32',
    datetime('now')
),
(
    'Samsung 990 Pro 1TB NVMe',
    'Топ PCIe 4.0 M.2',
    'Накопители',
    11990,
    20,
    '7450 MB/s чтение, M.2 NVMe',
    datetime('now')
),
(
    'Samsung 990 Pro 2TB NVMe',
    '2TB PCIe 4.0 M.2',
    'Накопители',
    21990,
    10,
    '7450 MB/s чтение, M.2 NVMe',
    datetime('now')
),
(
    'WD Black SN850X 2TB NVMe',
    'Игровой NVMe Gen4 с радиатором',
    'Накопители',
    20990,
    8,
    '7300 MB/s чтение, Heatsink',
    datetime('now')
),
(
    'Crucial P5 Plus 1TB NVMe',
    'Средний сегмент PCIe 4.0',
    'Накопители',
    8990,
    25,
    '6600 MB/s чтение, M.2',
    datetime('now')
),
(
    'Kingston NV2 1TB NVMe',
    'Бюджетный NVMe Gen3/4',
    'Накопители',
    5490,
    40,
    '3500 MB/s чтение, M.2',
    datetime('now')
),
(
    'Seagate Barracuda 4TB HDD',
    'Объёмный HDD для хранения',
    'Накопители',
    8490,
    20,
    '5400rpm, SATA 6Gb/s',
    datetime('now')
),
(
    'WD Red Plus 4TB HDD',
    'NAS-накопитель 4TB',
    'Накопители',
    9990,
    12,
    '5400rpm, CMR, SATA',
    datetime('now')
),
(
    'Toshiba 2TB HDD',
    'Бюджетный HDD 2TB',
    'Накопители',
    4990,
    35,
    '7200rpm, SATA 6Gb/s',
    datetime('now')
),
(
    'Samsung 870 QVO 1TB SATA',
    'SATA SSD 2.5 дюйма',
    'Накопители',
    7490,
    30,
    '560 MB/s чтение, SATA',
    datetime('now')
),
(
    'Crucial MX500 500GB SATA',
    'Надёжный SATA SSD',
    'Накопители',
    4490,
    45,
    '560 MB/s чтение, 2.5 дюйма',
    datetime('now')
),
(
    'WD Blue 1TB NVMe PCIe 3.0',
    'Базовый PCIe 3.0 M.2',
    'Накопители',
    6490,
    28,
    '3430 MB/s чтение, M.2',
    datetime('now')
),
(
    'Seagate FireCuda 530 2TB',
    'PCIe 4.0, совместим с PS5',
    'Накопители',
    19990,
    7,
    '7300 MB/s чтение, M.2',
    datetime('now')
),
(
    'Kingston KC600 256GB SATA',
    'Бюджетный SATA SSD 256GB',
    'Накопители',
    2990,
    60,
    '550 MB/s чтение, 2.5 дюйма',
    datetime('now')
),
(
    'ADATA Legend 800 1TB NVMe',
    'Бюджетный NVMe Gen4',
    'Накопители',
    5990,
    32,
    '3500 MB/s чтение, M.2',
    datetime('now')
),
(
    'Seagate Exos 8TB HDD',
    'Серверный объём 8TB CMR',
    'Накопители',
    16990,
    6,
    '7200rpm, SATA, Enterprise',
    datetime('now')
),
(
    'ASUS Prime Z790-P DDR5',
    'LGA1700, Z790, ATX',
    'Материнские платы',
    22990,
    12,
    'LGA1700, Z790, DDR5, 4x DIMM',
    datetime('now')
),
(
    'MSI MAG B760M Mortar DDR5',
    'LGA1700, B760, mATX',
    'Материнские платы',
    15990,
    18,
    'LGA1700, B760, DDR5, mATX',
    datetime('now')
),
(
    'Gigabyte B550 AORUS Elite V2',
    'AM4, B550, ATX DDR4',
    'Материнские платы',
    14990,
    14,
    'AM4, B550, DDR4, ATX',
    datetime('now')
),
(
    'ASUS ROG Strix X670E-F Gaming',
    'AM5, X670E, ATX флагман',
    'Материнские платы',
    52990,
    4,
    'AM5, X670E, DDR5, ATX',
    datetime('now')
),
(
    'MSI PRO B660M-A DDR4',
    'LGA1700, B660, mATX бюджет',
    'Материнские платы',
    9990,
    25,
    'LGA1700, B660, DDR4, mATX',
    datetime('now')
),
(
    'ASRock B650M PG Riptide',
    'AM5, B650, mATX',
    'Материнские платы',
    14990,
    16,
    'AM5, B650, DDR5, mATX',
    datetime('now')
),
(
    'Gigabyte Z790 AORUS Master',
    'LGA1700, Z790, ATX топ',
    'Материнские платы',
    49990,
    3,
    'LGA1700, Z790, DDR5, E-ATX',
    datetime('now')
),
(
    'ASUS TUF Gaming B550-Plus',
    'AM4, B550, ATX геймерская',
    'Материнские платы',
    12990,
    20,
    'AM4, B550, DDR4, ATX',
    datetime('now')
),
(
    'MSI MEG Z790 ACE',
    'LGA1700, Z790, E-ATX премиум',
    'Материнские платы',
    59990,
    2,
    'LGA1700, Z790, DDR5, E-ATX',
    datetime('now')
),
(
    'Gigabyte A520M DS3H',
    'AM4, A520, mATX бюджет',
    'Материнские платы',
    6990,
    40,
    'AM4, A520, DDR4, mATX',
    datetime('now')
),
(
    'ASUS Prime B650-Plus WiFi',
    'AM5, B650, ATX с WiFi 6',
    'Материнские платы',
    17490,
    15,
    'AM5, B650, DDR5, ATX, WiFi 6',
    datetime('now')
),
(
    'MSI B550 Gaming Gen3',
    'AM4, B550, ATX игровая',
    'Материнские платы',
    11990,
    22,
    'AM4, B550, DDR4, ATX',
    datetime('now')
),
(
    'Seasonic Focus GX-850',
    '850W, 80+ Gold, модульный',
    'Блоки питания',
    13990,
    10,
    '850W, 80+ Gold, Full Modular',
    datetime('now')
),
(
    'Corsair RM850x SHIFT',
    '850W, 80+ Gold, боковой разъём',
    'Блоки питания',
    14990,
    8,
    '850W, 80+ Gold, Semi-Modular',
    datetime('now')
),
(
    'be quiet! Straight Power 11 750W',
    '750W 80+ Platinum тихий',
    'Блоки питания',
    19990,
    6,
    '750W, 80+ Platinum, Modular',
    datetime('now')
),
(
    'Thermaltake Toughpower GF1 1000W',
    '1000W 80+ Gold модульный',
    'Блоки питания',
    18990,
    5,
    '1000W, 80+ Gold, Full Modular',
    datetime('now')
),
(
    'EVGA 650 BQ 650W Bronze',
    '650W 80+ Bronze полумодульный',
    'Блоки питания',
    7490,
    20,
    '650W, 80+ Bronze, Semi-Modular',
    datetime('now')
),
(
    'Chieftec A-135 600W Bronze',
    '600W 80+ Bronze бюджет',
    'Блоки питания',
    4990,
    30,
    '600W, 80+ Bronze, Non-Modular',
    datetime('now')
),
(
    'Cooler Master MWE Gold 750W',
    '750W 80+ Gold',
    'Блоки питания',
    9490,
    15,
    '750W, 80+ Gold, Non-Modular',
    datetime('now')
),
(
    'MSI MAG A750GL PCIE5',
    '750W PCIe 5.0 Gold',
    'Блоки питания',
    12990,
    12,
    '750W, 80+ Gold, Full Modular',
    datetime('now')
),
(
    'Seasonic Prime TX-1000',
    '1000W 80+ Titanium топ',
    'Блоки питания',
    29990,
    3,
    '1000W, 80+ Titanium, Modular',
    datetime('now')
),
(
    'Deepcool PK550D 550W',
    '550W 80+ Bronze бюджет',
    'Блоки питания',
    4490,
    40,
    '550W, 80+ Bronze, Non-Modular',
    datetime('now')
),
(
    'Noctua NH-U12A',
    'Башенный кулер 120mm топ',
    'Охлаждение',
    12490,
    8,
    'TDP 250W, AM4/AM5/LGA1700',
    datetime('now')
),
(
    'be quiet! Dark Rock Pro 4',
    'Двойная башня 250W',
    'Охлаждение',
    9990,
    10,
    'TDP 250W, LGA1700/AM4',
    datetime('now')
),
(
    'Deepcool AK620 ZERO DARK',
    'Двойная башня 6 трубок',
    'Охлаждение',
    6990,
    15,
    'TDP 260W, AM5/LGA1700, чёрный',
    datetime('now')
),
(
    'NZXT Kraken X63 280mm RGB',
    'СЖО 280mm с LCD экраном',
    'Охлаждение',
    22990,
    5,
    '280mm, AM5/LGA1700, RGB',
    datetime('now')
),
(
    'Corsair iCUE H100i Elite 240mm',
    'СЖО 240mm Capellix RGB',
    'Охлаждение',
    18990,
    6,
    '240mm, AM5/LGA1700, RGB',
    datetime('now')
),
(
    'Arctic Liquid Freezer III 240',
    'СЖО 240mm тихая',
    'Охлаждение',
    9990,
    12,
    '240mm, AM5/LGA1700',
    datetime('now')
),
(
    'Cooler Master MasterLiquid 360L',
    'СЖО 360mm ARGB',
    'Охлаждение',
    14990,
    4,
    '360mm, AM5/LGA1700, ARGB',
    datetime('now')
),
(
    'ID-Cooling SE-224-XTS ARGB',
    'Бюджетный кулер 120mm ARGB',
    'Охлаждение',
    2490,
    40,
    'TDP 200W, AM4/AM5/LGA1700',
    datetime('now')
),
(
    'Thermalright Peerless Assassin 120',
    'Двойная башня бюджет топ',
    'Охлаждение',
    4990,
    25,
    'TDP 260W, AM5/LGA1700',
    datetime('now')
),
(
    'be quiet! Pure Rock 2 Black',
    'Компактный 120mm кулер',
    'Охлаждение',
    3490,
    35,
    'TDP 150W, AM4/LGA1700',
    datetime('now')
),
(
    'Noctua NF-A12x25 PWM',
    'Вентилятор 120mm премиум',
    'Охлаждение',
    3290,
    50,
    '120mm, 2000rpm, 67.8 CFM',
    datetime('now')
),
(
    'Arctic P12 PWM PST 5 штук',
    'Комплект 5 вентиляторов 120mm',
    'Охлаждение',
    2990,
    60,
    '120mm, PWM Value Pack',
    datetime('now')
),
(
    'EK-AIO Elite 360 D-RGB',
    'СЖО 360mm EK Water Blocks',
    'Охлаждение',
    24990,
    3,
    '360mm, AM5/LGA1700, D-RGB',
    datetime('now')
),
(
    'Fractal Design North ATX',
    'Деревянные панели, стильный',
    'Корпуса',
    16990,
    7,
    'ATX, Walnut, USB-C, 2x140mm',
    datetime('now')
),
(
    'Lian Li O11 Dynamic EVO',
    'Двухкамерный ATX, стекло',
    'Корпуса',
    22990,
    5,
    'E-ATX, USB-C, 6 мест вент.',
    datetime('now')
),
(
    'NZXT H7 Flow White',
    'Отличная вентиляция, белый',
    'Корпуса',
    14990,
    8,
    'ATX, USB-C, 2x120mm, White',
    datetime('now')
),
(
    'Phanteks Eclipse G360A ARGB',
    'Геймерский ATX ARGB',
    'Корпуса',
    10990,
    12,
    'ATX, ARGB, 3x120mm вент.',
    datetime('now')
),
(
    'Corsair 5000D Airflow Black',
    'Максимальная вентиляция',
    'Корпуса',
    16990,
    6,
    'ATX, USB-C, 2x120mm вент.',
    datetime('now')
),
(
    'Deepcool CH510 WH',
    'Чистый дизайн, белый',
    'Корпуса',
    6990,
    18,
    'ATX, стекло, 1x120mm, White',
    datetime('now')
),
(
    'Cooler Master MasterBox Q300L',
    'mATX компактный',
    'Корпуса',
    4490,
    25,
    'mATX, акриловая панель',
    datetime('now')
),
(
    'Thermaltake Core P3 TG Open',
    'Open frame витрина',
    'Корпуса',
    13990,
    4,
    'E-ATX, Open Frame, 3x120mm',
    datetime('now')
),
(
    'Zalman Z10 Plus ARGB',
    'Бюджетный ATX с ARGB',
    'Корпуса',
    5490,
    20,
    'ATX, ARGB, 4x120mm вент.',
    datetime('now')
),
(
    'Silverstone FARA H1M',
    'Компактный mATX стекло',
    'Корпуса',
    3990,
    30,
    'mATX, стекло, 1x120mm',
    datetime('now')
),
(
    'MSI MAG Forge 100R ARGB',
    'Геймерский Mid Tower',
    'Корпуса',
    7990,
    14,
    'ATX, ARGB, 3x120mm вент.',
    datetime('now')
),
(
    'BitFenix Enso Mesh ATX',
    'Mesh фасад, хорошая вентиляция',
    'Корпуса',
    8490,
    11,
    'ATX, стекло, 1x120mm вент.',
    datetime('now')
),
(
    'LG 27GP850-B QHD 165Hz',
    'IPS 2560x1440, 27 дюймов',
    'Мониторы',
    34990,
    10,
    '27 дюймов, IPS, 165Hz, QHD',
    datetime('now')
),
(
    'Samsung Odyssey G7 32 240Hz',
    'Curved 240Hz QHD VA',
    'Мониторы',
    49990,
    5,
    '32 дюйма, VA, 240Hz, 1000R',
    datetime('now')
),
(
    'ASUS ProArt PA279CV 4K',
    '4K IPS для дизайнеров',
    'Мониторы',
    44990,
    4,
    '27 дюймов, IPS, 60Hz, 4K UHD',
    datetime('now')
),
(
    'MSI MAG 274QRF QD 165Hz',
    'QD-IPS 165Hz, QHD',
    'Мониторы',
    36990,
    7,
    '27 дюймов, QD-IPS, 165Hz, QHD',
    datetime('now')
),
(
    'AOC 24G2 FHD 144Hz IPS',
    'Геймерский IPS бюджет',
    'Мониторы',
    16990,
    20,
    '24 дюйма, IPS, 144Hz, FHD',
    datetime('now')
),
(
    'Philips 275E2FAE 4K 60Hz',
    '4K IPS офисный',
    'Мониторы',
    27990,
    8,
    '27 дюймов, IPS, 60Hz, 4K',
    datetime('now')
),
(
    'BenQ EX2780Q QHD 144Hz',
    'HDRi со встроенными динамиками',
    'Мониторы',
    38990,
    6,
    '27 дюймов, IPS, 144Hz, QHD',
    datetime('now')
),
(
    'Gigabyte M27Q SS-IPS 170Hz',
    'SS-IPS 170Hz KVM switch',
    'Мониторы',
    31990,
    9,
    '27 дюймов, SS-IPS, 170Hz, QHD',
    datetime('now')
),
(
    'Dell U2723DE USB-C Hub',
    '27 дюймов, USB-C док',
    'Мониторы',
    52990,
    3,
    '27 дюймов, IPS, 60Hz, QHD',
    datetime('now')
),
(
    'Acer Nitro XV252Q 360Hz',
    '360Hz для киберспорта',
    'Мониторы',
    41990,
    5,
    '25 дюймов, IPS, 360Hz, FHD',
    datetime('now')
),
(
    'Logitech G Pro X Superlight 2',
    'Беспроводная мышь 60г',
    'Периферия',
    12990,
    12,
    'HERO 25K сенсор, 5 кнопок',
    datetime('now')
),
(
    'Razer DeathAdder V3 Pro',
    'Беспроводная эргономичная',
    'Периферия',
    11990,
    10,
    'Focus Pro 30K, 5 кнопок',
    datetime('now')
),
(
    'SteelSeries Rival 650 Wireless',
    'Беспроводная с грузиками',
    'Периферия',
    9990,
    8,
    'TrueMove3+, 12 кнопок',
    datetime('now')
),
(
    'Zowie EC2-C',
    'Для CS:GO без RGB',
    'Периферия',
    5990,
    18,
    '3360 Optical, 5 кнопок, чёрный',
    datetime('now')
),
(
    'Glorious Model O2 Wireless',
    'Лёгкая 59г honeycomb',
    'Периферия',
    7990,
    15,
    'BAMF2 26K, 6 кнопок',
    datetime('now')
),
(
    'Corsair Dark Core RGB Pro',
    'Беспроводная с Qi зарядкой',
    'Периферия',
    8990,
    10,
    'PixArt PMW3392, Qi Charge',
    datetime('now')
),
(
    'HyperX Pulsefire Haste 2',
    'Ultra-light 53г honeycomb',
    'Периферия',
    4990,
    25,
    '26K DPI, Honeycomb корпус',
    datetime('now')
),
(
    'ASUS ROG Gladius III Wireless',
    '3 режима подключения',
    'Периферия',
    9490,
    9,
    'AimPoint Pro 36K',
    datetime('now')
),
(
    'Logitech G915 TKL Wireless',
    'Беспроводная тонкая TKL',
    'Периферия',
    14990,
    8,
    'GL Tactile, RGB, Bluetooth',
    datetime('now')
),
(
    'Razer BlackWidow V4 Pro',
    'Беспроводная полноразмерная',
    'Периферия',
    19990,
    5,
    'Yellow Switch, Chroma RGB',
    datetime('now')
),
(
    'Corsair K70 RGB Pro MX Speed',
    'Cherry MX Speed, Al основа',
    'Периферия',
    12990,
    10,
    'Cherry MX Speed, RGB, Al',
    datetime('now')
),
(
    'SteelSeries Apex Pro TKL',
    'Регулируемые переключатели',
    'Периферия',
    17490,
    6,
    'OmniPoint 2.0, OLED дисплей',
    datetime('now')
),
(
    'HyperX Alloy Origins 65',
    'Компактная 65%, Al',
    'Периферия',
    8490,
    15,
    'HyperX Aqua Switch, RGB, Al',
    datetime('now')
),
(
    'Keychron Q3 Max Wireless QMK',
    'Gasket-mount беспроводная',
    'Периферия',
    15990,
    7,
    'Gateron Jupiter Yellow, Al',
    datetime('now')
),
(
    'Ducky One 3 TKL Silent',
    'Premium PBT, тихие свитчи',
    'Периферия',
    11990,
    12,
    'Cherry MX Silent Red, PBT',
    datetime('now')
),
(
    'Logitech G Pro X 60 Lightspeed',
    '60% ультролёгкая беспроводная',
    'Периферия',
    9990,
    14,
    'GX Blue Clicky, RGB',
    datetime('now')
),
(
    'ASUS ROG Claymore II',
    '100% со съёмным NumPad',
    'Периферия',
    22990,
    4,
    'ROG RX Optical, RGB',
    datetime('now')
),
(
    'ASUS RT-AX86U Pro Wi-Fi 6',
    'Wi-Fi 6 AiMesh игровой',
    'Сетевое оборудование',
    12990,
    8,
    'AX5700, 4 порта LAN, USB 3.0',
    datetime('now')
),
(
    'TP-Link Archer AX73 Wi-Fi 6',
    'Wi-Fi 6, 6 антенн',
    'Сетевое оборудование',
    8990,
    12,
    'AX5400, 2.4+5GHz, USB 3.0',
    datetime('now')
),
(
    'Keenetic Giga SE Wi-Fi 6',
    'Wi-Fi 6 двухдиапазонный',
    'Сетевое оборудование',
    7490,
    15,
    'AX1800, 4 порта LAN, USB',
    datetime('now')
),
(
    'Netgear Nighthawk RS700S Wi-Fi 7',
    'Wi-Fi 7 триапазонный топ',
    'Сетевое оборудование',
    39990,
    3,
    'BE19000, 2.4+5+6GHz',
    datetime('now')
),
(
    'TP-Link TL-SG108 Gigabit 8-port',
    'Гигабитный свитч 8 портов',
    'Сетевое оборудование',
    2490,
    40,
    '8x1000Mbps, настольный',
    datetime('now')
),
(
    'D-Link DGS-1016D Gigabit 16-port',
    'Гигабитный свитч 16 портов',
    'Сетевое оборудование',
    7990,
    10,
    '16x1000Mbps, 19 дюймов',
    datetime('now')
),
(
    'ASUS PCE-AX3000 Wi-Fi 6 PCIe',
    'Wi-Fi 6 PCIe адаптер для ПК',
    'Сетевое оборудование',
    4990,
    20,
    'AX3000, Bluetooth 5.0',
    datetime('now')
),
(
    'TP-Link UB500 Bluetooth 5.0 Nano',
    'Bluetooth Nano USB адаптер',
    'Сетевое оборудование',
    890,
    100,
    'Bluetooth 5.0, USB Nano',
    datetime('now')
),
(
    'TP-Link TL-PA8033P Powerline',
    'Сеть через электропроводку 2шт',
    'Сетевое оборудование',
    4490,
    15,
    '1300Mbps, Gigabit LAN, 2-pack',
    datetime('now')
),
(
    'Ubiquiti UniFi AP U6 Lite PoE',
    'Точка доступа Wi-Fi 6 PoE',
    'Сетевое оборудование',
    9990,
    7,
    'AX1500, PoE, 4x4 MIMO',
    datetime('now')
);