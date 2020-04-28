# java 中的位运算

因为在java源码中经常看到位运算，每次看都会忘掉符号的意思，这边就先做个记录

A = 0011 1100 => 60

B = 0000 1101 => 13

-------

| 操作符   |     描述      |  例子 |
|----------|-------------|:------|
|& 按位与运算符| 如果相对应位都是1，则结果为1，否则为0 | （A&B），得到12，即0000 1100|
|l 按位或运算符| 如果相对应位都是0，则结果为0，否则为1 | （AlB），得到61，即0011 1101|
|^ 异或运算符| 如果相对应位值相同，则结果为0，否则为1 | （A^B），得到49，即0011 0001|
|~ 取反运算符| 按位取反运算符，翻转操作数的每一位，即0变成1,1变成0|（~A）得到-61，即11100 0011|
|<<| 按位左移运算符。左操作数按位左移右操作数指定的位数|A<<2得到240，即1111 0000|
|>>| 按位右移运算符。左操作数按位右移右操作数指定的位数|A>>2得到15，即1111|
|>>>| 按位右移补零操作符。左操作数的值按右操作数指定的位数右移，移动得到的空位以零填充|A>>>2得到15即0000 1111|


# 优先级

**下表中具有最高优先级的运算符在的表的最上面，最低优先级的在表的底部。**

<table class="reference">
	<tbody>
		<tr>
			<th style="width:66px;">
				类别 </th>
			<th style="width:274px;">
				操作符</th>
			<th style="width:132px;">
				关联性 </th>
		</tr>
		<tr>
			<td style="width:66px;">
				后缀</td>
			<td style="width:274px;">
				() [] . (点操作符)</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				一元</td>
			<td style="width:274px;">
				+ + - ！〜</td>
			<td style="width:132px;">
				从右到左</td>
		</tr>
		<tr>
			<td style="width:66px;">
				乘性&nbsp;</td>
			<td style="width:274px;">
				* /％</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				加性&nbsp;</td>
			<td style="width:274px;">
				+ -</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				移位&nbsp;</td>
			<td style="width:274px;">
				&gt;&gt; &gt;&gt;&gt; &nbsp;&lt;&lt;&nbsp;</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				关系&nbsp;</td>
			<td style="width:274px;">
				&gt;&gt; = &lt;&lt; =&nbsp;</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				相等&nbsp;</td>
			<td style="width:274px;">
				==&nbsp; !=</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				按位与</td>
			<td style="width:274px;">
				＆</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				按位异或</td>
			<td style="width:274px;">
				^</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				按位或</td>
			<td style="width:274px;">
				|</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				逻辑与</td>
			<td style="width:274px;">
				&amp;&amp;</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				逻辑或</td>
			<td style="width:274px;">
				| |</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
		<tr>
			<td style="width:66px;">
				条件</td>
			<td style="width:274px;">
				？：</td>
			<td style="width:132px;">
				从右到左</td>
		</tr>
		<tr>
			<td style="width:66px;">
				赋值</td>
			<td style="width:274px;">
				= + = - = * = / =％= &gt;&gt; = &lt;&lt; =＆= ^ = | =</td>
			<td style="width:132px;">
				从右到左</td>
		</tr>
		<tr>
			<td style="width:66px;">
				逗号</td>
			<td style="width:274px;">
				，</td>
			<td style="width:132px;">
				左到右</td>
		</tr>
	</tbody>
</table>			
