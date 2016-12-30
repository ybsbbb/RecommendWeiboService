/**
 * 
 */
package cn.edu.bjtu.weibo.service.serviceImpl.recommendWeiboServiceHelper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cn.edu.bjtu.weibo.dao.RecommendDAO;
import cn.edu.bjtu.weibo.dao.UserDAO;

/**
 * 创建这个类，调用<code>recommend()</code>或<code>recommend(int num)</code>方法即可将推荐结果写入数据库<br>
 * 需要每隔一段时间调用一次来更新用户的推荐列表，比如10分钟
 * @author yangbo
 *
 */
public class RecommendEngine {
	
	/**
	 * 推荐算法逻辑处理部分的对象
	 */
	UserLabelWeiboGraph graph;
	/**
	 * 过滤推荐结果的对象
	 */
	RecommendListFilter filter;
	/**
	 * 所有注册用户
	 */
	List<String> allusers;
	
	@Autowired
	UserDAO userDAO;
	@Autowired
	RecommendDAO recommendDAO;

	/**
	 * 构造器，初始化算法和过滤器
	 */
	public RecommendEngine(){
		graph = new UserLabelWeiboGraph();
		filter = new RecommendListFilter();
	}
	
	/**
	 * 使用推荐算法完成推荐
	 * @param num 候选推荐数量
	 */
	public void recommend(int num){
		allusers = userDAO.getTotalUserId();//取所有用户
		if(allusers != null){//返回值不为空则运行推荐算法，否则不运行
			for(String u : allusers){//将所有用户插入图中
				graph.updateUser(u);
			}
			graph.updateWeibo(num);	//将数据库前num条插入取出插入图中
			for(String u : allusers){//生成所有用户的推荐列表并且写入数据库
				List<String> originalList = graph.RecommendWeibo(u);//生成指定用户的初始推荐列表
				List<String> filteredList = filter.getFilteredRecommendList(u, originalList);//对初始推荐列表进行过滤
				this.insertIntoDb(u, filteredList);//将最终推荐列表写入数据库
			}
			return;
		}
		System.out.println("recommend(int num)：数据库操作userDAO.getTotalUserId()返回空");
	}
	/**
	 * 使用推荐算法完成推荐，候选数量为100条
	 */
	public void recommend(){
		allusers = userDAO.getTotalUserId();
		if(allusers != null){
			for(String u : allusers){
				graph.updateUser(u);
			}
			graph.updateWeibo(100);//默认数量
			for(String u : allusers){
				List<String> originalList = graph.RecommendWeibo(u);
				List<String> filteredList = filter.getFilteredRecommendList(u, originalList);
				this.insertIntoDb(u, filteredList);
			}
			return;
		}
		System.out.println("recommend()：数据库操作userDAO.getTotalUserId()返回空");
	}
	
	/**
	 * 将最终推荐结果写入数据库
	 * @param uid
	 * @param wlist
	 */
	private void insertIntoDb(String uid,List<String> wlist){
		boolean resOfDel = recommendDAO.deleteRecommendWeibo(uid);//旧推荐列表删除
		if(resOfDel){
			System.out.println("删除用户"+ uid +"旧推荐列表成功");
		}else{
			System.out.println("删除用户"+ uid +"旧推荐列表失败");
		}
		boolean resOfSet = recommendDAO.setRecommendWeiboList(uid, wlist);//新推荐列表写入
		if(resOfSet){
			System.out.println("设置用户"+ uid +"新推荐列表成功");
		}else{
			System.out.println("设置用户"+ uid +"新推荐列表失败");
		}
	}
}
