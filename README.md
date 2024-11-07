# java-explore-with-me

## ЧАСТЬ 3 - КОММЕНТАРИИ К СОБЫТИЯМ
**PULL-REQUEST:** https://github.com/kirshumir01/java-explore-with-me/pull/3

### Дополнительная функциональности приложения разработана по следующим эндпоинтам:

#### 1. PRIVATE
**1.1 POST: /users/{userId}/comments**
- получает запрос от пользователя на публикацию комментария или ответа к комментарию;
- максимальный размер - 1000 символов.

**1.2 GET: /users/{userId}/comments**
- возвращает все комментарии пользователя к событию.

**1.3 PATCH: /users/{userId}/comments/{commentId}**
- получает запрос от автора на редактирование комментария с учетом следующих условий:
  - комментарий возможно отредактировать не позднее чем через 2 часа после публикации,
  - нередактируемый и заблокированный комментарий редактированию не подлежит,
  - комментарий допускается редактировать не более 2 раз,
  - комментарий подлежит редактированию только автором.

**1.4 DELETE: /users/{userId}/comments/{commentId}**
- получает запрос от автора на удаление комментария с ответами (при наличии).

#### 2. ADMIN
**2.1 GET: /admin/comments**
- поиск всех комментариев с возможностью фильтрации по периоду публикации, идентификаторам автора и события,
по типу (родительские, ответы или родительские с ответами), по статусам состояния;
- возвращает полную информацию о комментариях;
- в случае, если по заданным фильтрам не найдено ни одного комментария, возвращает пустой список.

**2.2 GET: /admin/comments/{commentId}**
- возвращает полную информацию о комментарии и ответы к нему.

**2.3 PATCH: /admin/comments**
- получает запрос от администратора на модерацию комментария по содержанию с возможностью блокировки комментария.

#### 3. PUBLIC
**3.1 GET: /comments/{eventId}**
- поиск комментариев к событию с возможностью фильтрации по содержанию, по периоду публикации c возможностью сортировки
по дате публикации;
- возвращает только корневые (родительские) и незаблокированные администратором комментарии без ответов;
- в случае, если по заданным фильтрам не найдено ни одного комментария, возвращает пустой список.

### Схема базы данных приложения (ER-диаграмма)
![ER-diagram](https://github.com/kirshumir01/java-explore-with-me/blob/feature_comments/EWM-diagram.png)