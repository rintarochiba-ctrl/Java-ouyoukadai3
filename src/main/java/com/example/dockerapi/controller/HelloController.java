package com.example.dockerapi.controller;

import java.sql.PreparedStatement;
import java.sql.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

class User{
    private int id;
    private String name;
    private String email;

    public User(){}

    public User(int id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

@RestController
public class HelloController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Docker World!";
    }

    @GetMapping("/hoge")
    public String sayHoge() {
        return "hogehogehoge";
    }

    @GetMapping("/check-db")
    public String checkDbConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class); // MySQLへの接続確認
            return "Database connection is successful!";
        } catch (Exception e) {
            return "Database connection failed!";
        }
    }

    //要件1:エンドポイントにGET通信をしたときに、レスポンスが返却されるようにする
    //エンドポイント：http://localhost:8080/users/{user_id}
    @GetMapping ("/users/{id}")//GetMappingはURL検索でアクセスされた時に実行する
    public User getUserById(@PathVariable int id){//メソッドを宣言　処理が終わるとEmployeeのリストを返す PathVariableは{}の値を変数名に格納
        String sql = "SELECT id,name,email FROM employee WHERE id = ?";//DBに命令するSQL文を変数sqlに格納
        User user = jdbcTemplate.queryForObject(//jdbcTemplate.queryForObject()はDBに対してSQLを実行する
            sql,//第一引数：命令文を渡す　BeanPropertyRowMapperはDBから返ってきたデータを列名とクラスのプロパティを結びつける
            BeanPropertyRowMapper.newInstance(User.class),//第二引数：列名とフィールド名を対応付けてjavaのデータに変換
            id//第三引数以降：命令文の？(プレースホルダー)部分に順に代入　newInstanceメソッドはクラスを指定することでこのクラスの形に自動変換してくれるインスタンスを作る
        );
        return user;//得られたデータリストをwebブラウザやcurlに返す
    }

    //要件2:エンドポイントにPOST通信したときに、新たにデータが追加される。作成されたデータが返却されるようにする
    //エンドポイント：http://localhost:8080/users
    @PostMapping("/users")//usersにPOST(登録)リクエストが来たら以下を実行
    @ResponseStatus(HttpStatus.CREATED) // 成功時に 201 Created を返す設定
    public User createUser(@RequestBody User user) {//Userオブジェクトを返すcreateUserメソッド　RequestBodyでリクエスト内のJSONを見つける Userクラスから空のuserオブジェクト作成
        String sql = "INSERT INTO employee (name, email) VALUES (?, ?)";//DBに渡すSQL文

        // 自動採番された ID を受け取るための準備
        KeyHolder keyHolder = new GeneratedKeyHolder();//KeyHolder(インターフェース)=役割、GeneratedKeyHolder(クラス)=役割を実行する本体

        // SQLの実行と ID の取得
        jdbcTemplate.update(connection -> {//データの更新を行うためのjdbcTemplateの命令　connectionはDBの通信接続
            //↓インターフェース(役割)：安全にSQLを実行するための命令書,prepareStatementメソッドは以下の条件でSQLを発行する準備をする
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);//第一引数：SQL文、第二引数：データを入れたら自動採番されたidを返す処理
            ps.setString(1, user.getName());//PSインターフェースのメソッドsetStringはpsの〇番目の？に文字列をセットするという命令
            ps.setString(2, user.getEmail());
            return ps;//設定が完了したps(SQL)を実行
        }, keyHolder);//idをkeyHolderに保存

        // 自動生成された ID を取得して、受け取った user オブジェクトの id にセット
        int newId = keyHolder.getKey().intValue();//KeyHolderインターフェースのgetKeyメソッド：自動採番したIDを取り出す NumberクラスのintValue：javaのint型に変換
        user.setId(newId);

        // IDがセットされた状態の user オブジェクトを返す（自動でJSON化される）
        return user;
    }

    //要件3:エンドポイントにPUT通信をしたときに、更新されたレスポンスが返却されるようにする
    //エンドポイント：http://localhost:8080/users/{user_id}
    @PutMapping("/users/{user_id}")//リクエスト送るエンドポイントの指定
    @ResponseStatus(HttpStatus.NO_CONTENT)//処理が成功した時にHTTPステータスに表示
    public void updateUser(@PathVariable("user_id") int userId, @RequestBody User user) {//responseのBodyに何も返さない,PathVariableでuser_idをuserIdに格納+リクエスト時のBody内容をJSONのUserオブジェクトにしてuserに格納
        String sql = "UPDATE employee SET name = ?, email = ? WHERE id = ?";//指定したidのnameとemailを更新するSQL文
        jdbcTemplate.update(sql, user.getName(), user.getEmail(), userId);//sqlの実行,第2から第4引数までは？に入る値
    }

    //要件4:エンドポイントにDELETE通信をしたときに、対象のユーザーを削除する
    //エンドポイント：http://localhost:8080/users/{user_id}
    @DeleteMapping("/users/{user_id}")//リクエスト送るエンドポイントの指定
    public ResponseEntity<Void> deleteUser(@PathVariable("user_id") int userId) {//ResponseEntityクラス：レスポンスのステータスを動的に制御するときの返り値として利用
        String sql = "DELETE FROM employee WHERE id = ?";//DBに渡すSQL文
        // 削除を実行し、実際に削除された件数（行数）を受け取る
        int rowsAffected = jdbcTemplate.update(sql, userId);//エンドポイントのidを引数にSQL実行
        // 削除された件数が 0 の場合は、対象のユーザーが存在しなかったことを意味する
        if (rowsAffected == 0) {
            return ResponseEntity.notFound().build(); // 404 Not Found を返すメソッドの並び
        }
        // 削除に成功した場合は 200 OK を返す
        return ResponseEntity.ok().build(); // 200 OK を返すメソッドの並び
    }
}
