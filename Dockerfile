# ベースイメージ（amd64/arm64 両対応の OpenJDK）
FROM eclipse-temurin:17-jdk

# 作業ディレクトリを設定
RUN mkdir -p /app
WORKDIR /app

# 必要なパッケージをインストール（maven + ファイル監視用の inotify-tools）
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven inotify-tools \
    && rm -rf /var/lib/apt/lists/*

# 依存関係を先にダウンロードしてキャッシュを利用
COPY pom.xml /app
RUN mvn dependency:go-offline

# プロジェクトのソースコードをコピー
COPY src /app/src

# Maven のビルド（キャッシュを活用）
RUN mvn clean install

# ホットリロードを有効にする環境変数を設定
ENV JAVA_OPTS="-Dspring.devtools.restart.enabled=true -Dspring.devtools.livereload.enabled=true"

# src の変更を監視して自動でコンパイル（→ DevTools が再起動）しつつ、アプリを起動
CMD ["sh", "-c", "(while inotifywait -q -r -e modify,create,delete,move /app/src; do echo '--- ソース変更を検知: 再コンパイルします ---'; mvn -q compile; done) & mvn spring-boot:run -Dspring-boot.run.profiles=dev $JAVA_OPTS"]
