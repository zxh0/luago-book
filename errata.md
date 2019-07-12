# 勘误表

页数		| 章节		| 位置								| 原文									| 更正									| 读者					| 更正版次
------- | --------- | --------------------------------- | ------------------------------------- | ------------------------------------- | --------------------- | ---------
VII		| 前言		| 第二段话							| 前18章的代表							| 前18章的代码							| ![moon][moon]			| 第二次印刷 
11		| 2.2		| 第一段话第二行						| 。。。件：第二，							| 。。。件；第二，							| ![moon][moon]			| 第二次印刷 
12		| 2.2.1		| 第三段话							| 调式信息								| 调试信息								| ![泡泡][泡泡]			| 第二次印刷 
21		| 2.3.3		|[末尾倒数第7行代码][p21]				| `CSZIET_SIZE`							| `CSIZET_SIZE`							| ![小灰先生][小灰先生]	| 第二次印刷 
26		| 2.3.4		| 10.行号表							| 示例里第二行的波浪线少标记了两个0			|										| ![逍遥][逍遥]			|
29		| 2.4.2		|[末尾倒数第3行代码][p29]				| `CSZIET_SIZE`							| `CSIZET_SIZE`							| ![小灰先生][小灰先生]	| 第二次印刷 
104		| 6.2.4		|[LEN指令实现代码][p104]				| `func _len(...)`						| `func length(...)`					| ![小灰先生][小灰先生]	| 第二次印刷 
106		| 6.2.5		| 第一段话第二行						| 如果。。。匹配，则跳过下一条指令。			| 如果。。。不匹配，则跳过下一条指令。		| ![开心就好][开心就好]	| 第二次印刷 
109		| 6.2.6		| 最后一段话第二行						| 如果一致，则跳过下一条指令。				| 如果不一致，则跳过下一条指令。				| ![balus][balus]		| 第二次印刷 
111		| 6.2.7		| 例子								| `for i=1,2,100 do f() end`			| `for i=1,100,2 do f() end`			| ![balus][balus]		| 第二次印刷 
113		| 6.2.7		| 本页第二段话						| 请读者在inst_operators.go文件里。。。	| 请读者在inst_for.go文件里。。。			| ![小海星][小海星]		|
122		| 7.2		|[多处][p122]						| `_floatToIntger()`					| `_floatToInteger()`					| ![moon][moon]			| 第二次印刷 
127		| 7.3.1		| 倒数第二段话							| 。。。抽取成`GetTable()`方法，		| 。。。抽取成`getTable()`方法，			| ![balus][balus]		|
129		| 7.3.2		| 最后一段话							| 。。。抽取成`SetTable()`方法，			| 。。。抽取成`setTable()`方法，			| ![balus][balus]		|
144		| 8.2.1		|[luaStack结构体][p144]				| `closure *luaClosure`					| `closure *closure`					| ![小灰先生][小灰先生]	| 第二次印刷 
145		| 8.2.1		| 最后一段话							| 。。。vararg字段用于。。。				| 。。。varargs字段用于。。。				| ![moon][moon]			| 第二次印刷 
152		| 8.4.1		| 本页倒数第二段话第一行					| 。。。（iBx模式）。。。				| 。。。（iABx模式）。。。					| ![balus][balus]		|
160		| 8.4.6		| 第一段话第二行						| 。。。面相对象体系。						| 。。。面向对象体系。						| ![泡泡][泡泡]			| 第二次印刷 
177		| 9.3.1		| `GetGlobal()`第二种实现				| `return self.GetField(t, name)`		| `return self.GetField(-1, name)`		| ![泡泡][泡泡]			| 第二次印刷 
193		| 10.2.3	|[`PushGoClosure()`方法][p193]		| `closure.upvals[n-1] = &upvalue{&val}`|`closure.upvals[i-1] = &upvalue{&val}` | ![孤舟钓客][孤舟钓客]	| 第二次印刷
227		| 12.2		| `nextKey()`方法					| `self.changed = false`多余				| 换成空行								| ![zedongh][zedongh]	| 第二次印刷
236		| 13.1		| 倒数第二段话第一行					| 。。。允许我们再有且仅。。。				| 。。。允许我们在有且仅。。。				| ![泡泡][泡泡]			| 第二次印刷 
252		| 14.3		|[`NextToken()`方法][p252a]			| `case ';': ... return ... ""`			| `case ';': ... return ... ";"`		| ![小灰先生][小灰先生]	| 第二次印刷 
252		| 14.3		|[`NextToken()`方法][p252b]			| `case ',': ... return ... ""`			| `case ',': ... return ... ","`		| ![小灰先生][小灰先生]	| 第二次印刷 
263		| 14.3.8	|[`NextToken()`方法][p263]和下面的文字	| `isLatter()`							| `isLetter()`							| ![泡泡][泡泡]			| 第二次印刷 
263		| 14.3.8	|[`NextToken()`方法][p263b]			| `return line, ...`					| `return self.line, ...`				| ![zedongh][zedongh]	| 第二次印刷
290		| 16.1.2	| 图16-2								| 上下文无言								| 上下文无关								| ![moon][moon]			| 第二次印刷 
304		| 16.4		|[表达式EBNF][p304]					| 少了exp3								| 请参考源代码注释							| ![无期相聚][无期相聚]	| 第二次印刷
369		| 19.1		| 第二段话							| 。。。定义数据库开启函数，				| 。。。定义数学库开启函数，				| ![泡泡][泡泡]			| 第二次印刷 
377		| 19.5		| 第三段话第二行						| 。。。创建lib_os.go.go文件，				| 。。。创建lib_os.go文件，				| ![泡泡][泡泡]			| 第二次印刷 

[moon]: readers/moon.png "moon"
[泡泡]: readers/paopao.jpeg "泡泡"
[小灰先生]: readers/小灰先生.jpeg "小灰先生"
[孤舟钓客]: readers/孤舟钓客.jpeg "孤舟钓客"
[开心就好]: readers/开心就好.jpeg "开心就好"
[无期相聚]: readers/无期相聚.jpeg "无期相聚"
[zedongh]: readers/zedongh.jpeg "https://github.com/zedongh"
[balus]: readers/balus.jpeg "https://github.com/BalusChen"
[小海星]: readers/小海星.jpeg "小海星"
[逍遥]: readers/逍遥.jpeg "逍遥"

[p21]:   code/go/ch02/src/luago/binchunk/binary_chunk.go#L9
[p29]:   code/go/ch02/src/luago/binchunk/reader.go#L70
[p104]:  code/go/ch06/src/luago/vm/inst_operators.go#L100
[p122]:  code/go/ch07/src/luago/state/lua_table.go#L36
[p144]:  code/go/ch08/src/luago/state/lua_stack.go#L8
[p193]:  code/go/ch10/src/luago/state/api_push.go#L47
[p252a]: code/go/ch14/src/luago/compiler/lexer/lexer.go#L80
[p252b]: code/go/ch14/src/luago/compiler/lexer/lexer.go#L83
[p263]:  code/go/ch14/src/luago/compiler/lexer/lexer.go#L204
[p263b]: code/go/ch14/src/luago/compiler/lexer/lexer.go#L207
[p304]:  code/go/ch16/src/luago/compiler/parser/parse_exp.go#L33
