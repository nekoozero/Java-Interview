# SQL 优化

### 负向查询不能使用索引

```sql
select name from user where id not in (1,3,4);
```
应该修改为:

```
select name from user where id in (2,5,6);
```

### 前导模糊查询不能使用索引
如:

```sql
select name from user where name like '%zhangsan'
```

非前导则可以:
```sql
select name from user where name like 'zhangsan%'
```
建议可以考虑使用 `Lucene` 等全文索引工具来代替频繁的模糊查询。

### 使用多列索引的查询语句
对于多列索引，只有查询条件中使用了这些字段中第一个字段时，索引才会被使用。也就是下面的最左前缀问题。

创建索引。
```sql
CREATE INDEX index_id_price ON fruits(f_id,f_price);
```

查询：
```sql
SELECT * FROM fruits WHERE f_id ='12';
```
这个查询会使用索引。

```sql
SELECT * FROM fruits WHERE f_price =250;
```
这个查询不会使用索引。只有查询条件中使用了f_id字段才会使index_id_price索引起作用。

### 使用or关键字查询语句
查询语句的查询条件中只有or关键字，且or前后的两个条件中的列都是索引时，查询中才能使用索引，否则，查询将不使用索引。

### 数据区分不明显的不建议创建索引

如 user 表中的性别字段，可以明显区分的才建议创建索引，如身份证等字段。

### 字段的默认值不要为 null
这样会带来和预期不一致的查询结果。

### 在字段上进行计算不能命中索引

```sql
select name from user where FROM_UNIXTIME(create_time) < CURDATE();
```

应该修改为:

```sql
select name from user where create_time < FROM_UNIXTIME(CURDATE());
```

### 最左前缀问题

如果给 user 表中的 username pwd 字段创建了复合索引那么使用以下SQL 都是可以命中索引:

```sql
select username from user where username='zhangsan' and pwd ='axsedf1sd'

select username from user where pwd ='axsedf1sd' and username='zhangsan'

select username from user where username='zhangsan'
```

但是使用

```sql
select username from user where pwd ='axsedf1sd'
```
是不能命中索引的。

### 如果明确知道只有一条记录返回

```sql
select name from user where username='zhangsan' limit 1
```
可以提高效率，可以让数据库停止游标移动。

### 不要让数据库帮我们做强制类型转换

```sql
select name from user where telno=18722222222
```
这样虽然可以查出数据，但是会导致全表扫描。

需要修改为
```
select name from user where telno='18722222222'
```

### 如果需要进行 join 的字段两表的字段类型要相同

不然也不会命中索引。

### 优化子查询

子查询虽然可以使查询语句很灵活，但执行效率不高。执行子查询时，Mysql 需要为内层查询语句建立一个临时表。然后外层查询语句从临时表中查询记录。查询完毕后，在撤销这些临时表，因此，子查询的速度会受到一定的影响。如果查询的数据量较大，这种影响就会随之增大。

在 Mysql 中，可以使用连接（join）查询来代替子查询。连接查询不需要建立临时表，其速度比子查询要快，如果查询中使用索引的话，性能会更好。连接之所以更有效率，是因为Mysql不需要在内存中创建临时表来完成查询工作。