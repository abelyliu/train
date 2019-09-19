火车票查询代码

输入起始站，目的站和日期

A->B->C->D
如果是起始站是b，目的站是c，则会查找b->c,a->c,a->d,b->d的车票
只会查找卧铺和硬座

main方法里的`String s = jsonArray.getString(jsonArray.size()-1);`这一行可以从1开始增，一直到数组越界异常为止，每增一次，换一辆车次查询
