PvpRealm
========

* Телепорт в Pvp мир и обратно, в исходную позицию, командой или предметом
* Pvp Logger - наказывает игроков, выходящих из игры во время Pvp
* Таблички восстановления маны, хп и сытости (Heroes), обратный отсчет игрокам поблизости, снятие всех эффектов
* Телепортация в случайную точку, из всех, подходящих по префиксу

Требует Heroes и опционально Towny, WorldGuard

Pvp мир
-------

`/pvpr enter <player>` - телепортировать игрока в пвп-мир (на позицию в конфиге)
`/pvpr return <player>` - вернуть игрока из пвп-мира на позицию, откуда он вошёл. Если ее по какой-то причине нет, будет использоваться дефолтная

* Если происходит ребут или игрок выходит в пвп мире, то его локация сохраняется в профиль (находятся в директории players)
* Если игрок телепортирован через другой плагин, всё равно отслеживается его перемещение
* Можно определить предмет для телепорта в пвп мир, во время его применения нельзя двигаться
* При возврате из Pvp мира с персонажа снимаются все эффекты (не Heroes)
* В Pvp мире игроки не получают опыт (Heroes) за мобов

Pvp logger
----------

Описание в конфиге

Точки телепорта
---------------

Суть в том, чтобы телепортировать игрока в случайную точку из заданных по префиксу  
Например, если есть точки: `tree1, spawn, tree2, bridge, treeeeee`  
То при телепорте по префиксу `tree`, будет случайно выбрана точка из `tree1, tree2, treeeeee`

`/pvpr bpoint add <point>` - сохранить текущую позицию, как точку телепорта  
`/pvpr bpoint list` - список точек телепорта  
`/pvpr bpoint remove <point>` - удалить точку телепорта  
`/pvpr bpoint info <point>` - посмотреть координаты точки телепорта  
`/pvpr bpoint tpprefix <player> <point_prefix>` - телепортировать игрока в случайную точку, из всех, начинающихся на <point_prefix>  
`/pvpr bpoint tp <player> <point>` - телепортировать игрока в указанную точку  

Pvp-таблички
------------

    [                     ]
    [     [countdown]     ]
    [                     ]
    [                     ]
Обратный отсчет всем игрокам, в радиусе 50. Полезно для арен.


    [                     ]
    [      [restore]      ]
    [                     ]
    [                     ]
Восстановление HP, маны и сытости (Heroes)


    [                     ]
    [     [rmeffects]     ]
    [                     ]
    [                     ]
Снятие всех эффектов

Конфигурация
------------

    debug: true  # вывод сообщений отладки в консоль
    lang: mccity  # какой .lang файл использовать для текстов

    pvp-world:
        enable: true
        world: Aertos_pvp
        disable-heroes-death-exp-loss: true  # отключить потерю опыта в пвп мире
        disable-heroes-mob-exp: true  # отключить получение опыта за мобов пвп мире
        disable-weather: true
        entry-loc:  # точка входа в пвп мир
            world: Aertos_pvp
            x: 1082
            y: 62.5
            z: 872
            pitch: 5
            yaw: 184
        default-return-loc:  # дефолтная точка возврата
            world: Aertos
            x: 3022.62
            y: 64.3
            z: -3085.66
            pitch: 5
            yaw: 0
        enter-scroll:
            enable: true
            item: 358-51  # предмет (id-data) для телепортации в пвп-мир
            consume: true  # расходуется при использовании?
            delay-sec: 10  # время применения
            broadcast-arrival: true  # если true, при прибытии в пвп мир игрока будет выведено серверное сообщение (из lang-файла)
    
    pvp-logger:
        enable: true
        enable-op: false  # применять пвп-логгер к ОПам?
        bypass-friendly: true  # Если true - будут игнорироваться мемберы группы и союзники Towny (если towny установлен)
        global: true  # Везде или только в пвп мире?
        message: true  # Серверное сообщение
        heroes-exp-penalty: 0  # Штраф опыта Heroes
        kill: true  # Убивать игрока
    
    death-nodrop-regions:  # Регионы WorldGuard, в которых при смерти не теряется инвентарь
      - id: arena_main
        world: Aertos_pvp

`/pvpr reload` - перегрузить конфигурацию и тексты из .lang файла


Пермишены
---------

`pvprealm.command` - доступ ко всем командам (op)  
`pvprealm.enterscroll` - возможность использовать свиток входа в пвп-мир (op)  
`pvprealm.placesign.rmeffects` - создавать таблички удаления эффектов (op)  
`pvprealm.placesign.countdown` - создавать таблички обратного отсчета (op)  
`pvprealm.placesign.restore` - создавать таблички регенерации (op)  
`pvprealm.bypass.returnrmeffects` - не снимать эффекты при возврате из pvp-мира (никто)  

Download
--------

Версия CraftBukkit: 1.4.5-R1.0  
https://dl.dropbox.com/u/14150510/dd/mccity/PvpRealm.jar