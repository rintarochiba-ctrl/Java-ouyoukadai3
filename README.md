### Java 課題共通雛形リポジトリ

1. このリポジトリを研修用のフォルダにクローンする

```
git clone https://github.com/YuYoshida7211/java-basic.git
```

2. mvn のインストール
3. java のインストール
4. docker コンテナを立ち上げる

```
// dockerビルド
docker compose build

// dockerをバックグラウンドで起動
docker compose up -d

```

### docker コンテナに入る

```
docker exec -it mysql-container bash

```

### コードの修正後以下コマンドでコンテナを立ち上げ直して、レスポンスを確認する

```

docker compose restart app

```

### mysql に入る

```

docker exec -it mysql-container mysql -uroot -proot

```

データベース選択

```

use db 名

```

テーブル作成

```

create table demo.users(id int,name varchar(10));

```

データ挿入

```

insert into users values (1, 'Yamada');

```

dump する方法

```

docker exec -i mysql-container mysqldump -u root -p demo > dump.sql

```
