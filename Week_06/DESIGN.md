##### 用户表

| 列名     | 数据类型 | 长度 | 主键 | 非空 | 注释     |
| -------- | -------- | ---- | ---- | ---- | -------- |
| id       | int      |      | Y    | Y    | 自增主键 |
| code     | varchar  | 15   |      | Y    | 用户编号 |
| name     | varchar  | 15   |      | Y    | 用户名   |
| avatar   | varchar  | 30   |      |      | 头像     |
| password | varchar  | 20   |      | Y    | 密码     |

##### 商品表

| 列名        | 数据类型 | 长度 | 主键 | 非空 | 注释     |
| ----------- | -------- | ---- | ---- | ---- | -------- |
| id          | int      |      | Y    | Y    | 自增主键 |
| goods_code  | varchar  | 15   |      | Y    | 商品编号 |
| goods_name  | varchar  | 30   |      | Y    | 商品名称 |
| goods_desc  | varchar  | 100  |      |      | 商品描述 |
| goods_cover | varchar  | 50   |      |      | 商品封面 |
| shop_id     |          |      |      |      | 商家id   |
| min_price   |          |      |      |      | 最低价格 |
| visits      |          |      |      |      | 访问量   |
| sale_count  |          |      |      |      | 成交数   |

##### 商家表

| 列名        | 数据类型 | 长度 | 主键 | 非空 | 注释     |
| ----------- | -------- | ---- | ---- | ---- | -------- |
| id          | int      |      | Y    | Y    | 自增主键 |
| name        | varchar  | 15   |      | Y    | 商品编号 |
| head        | varchar  | 30   |      | Y    | 商品名称 |
| goods_desc  | varchar  | 100  |      |      | 商品描述 |
| goods_cover | varchar  | 50   |      |      | 商品封面 |