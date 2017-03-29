# Spider
Java 爬虫，爬取天堂图片网http://www.ivsky.com的所有壁纸  
	首页  
		1.http://www.ivsky.com/bizhi/  
		2.http://www.ivsky.com/tupian/  
	两个首页要分别抓取，修改SpiderUtil.rootURL,并将正则表达式中所有“bizhi”改为“tupian”即可  

Main.java  
	
1.获取URL存入数据库 SpiderUtil.java  
	- 获取分类的URL信息存入数据库  
![Alt text](https://github.com/pokerfaceSad/Spider/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E5%A4%A7%E5%88%86%E7%B1%BB%E4%BF%A1%E6%81%AF.png)  
	- 获取每一个分类中所有的壁纸集URL信息存入数据库  
![Alt text](https://github.com/pokerfaceSad/Spider/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E5%A3%81%E7%BA%B8%E9%9B%86%E4%BF%A1%E6%81%AF.png)
	
2.从数据库中取出每一个分类的所有壁纸集URL多线程下载 Main.java downloadThread  
	- 每一个分类开启一个线程下载  
	- 按照分类名称和壁纸集名称将图片存入相应目录  
