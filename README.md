# Side Only plagin 

## Inlay Hints
Реализовано в [SideInlayProvider.java](src%2Fmain%2Fjava%2Fcom%2Fexbo%2Fsideonlyintelijiplugin%2FSideInlayProvider.java)

Чтобы добавить еще элементы, над которыми нужно отображать -> `SideInlayProvider.Collector.collect()`

Чтобы поменять формат отображения -> `SideInlayProvider.Collector.collect()`


## Inspection
Реализовано в [BadUsageInspection.java](src%2Fmain%2Fjava%2Fcom%2Fexbo%2Fsideonlyintelijiplugin%2FBadUsageInspection.java)

Чтобы добавить тип для проверки
1. Создать новый `check()` в `BadUsageInspection`
2. Написать новый `visit()` в `BadUsageInspection.Visiter` и зарегистрировать в нём `check()`

## Check Duplicate Inspection
Нету в тз, сделал для тестов. [DuplicateInspection.java](src%2Fmain%2Fjava%2Fcom%2Fexbo%2Fsideonlyintelijiplugin%2FDuplicateInspection.java)
Можно вырезать.

## Params
в `PsiUtils` и `DuplicateInspection` захардкожено местоположение `SIDE_ONLY_ANNOTATION`
