/**
 * 
 */
package cn.edu.bjtu.weibo.service.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.bjtu.weibo.algorithm.RecommendEngine;
import cn.edu.bjtu.weibo.dao.RecommendDAO;
import cn.edu.bjtu.weibo.dao.WeiboDAO;
import cn.edu.bjtu.weibo.model.Weibo;
import cn.edu.bjtu.weibo.service.RecommendWeiboService;

/**
 * 微博推荐的实现类
 * @author yangbo
 *
 */
@Service("recommendWeiboService")
public class RecommendWeiboServiceImpl implements RecommendWeiboService{

	@Autowired
	WeiboDAO weiboDAO;//DAO层对象
	@Autowired
	RecommendDAO recommendDAO;//DAO层对象
	
	public List<Weibo> getRecommentWeiboList(String userId, int pageIndex, int numberPerPage) {
		//从数据库中取出为该用户推荐的前numberPerPage条推荐,
		//第二个参数设置为0是因为每次都会把推荐过的微博从推荐列表删去，不论请求到第几页都是从第一页取
		List<String> wids = recommendDAO.getRecommendWeiboList(userId, 0, numberPerPage);
		//创建要返回的推荐微博列表wlist
		List<Weibo> wlist = new ArrayList<Weibo>();
		
		//如果返回的推荐列表不为空，根据推荐微博的id取到真正的微博，放入wlist
		if(wids != null){
			for(String id : wids){
				Weibo weibo = weiboDAO.getWeibo(id);
				if(weibo != null){
					wlist.add(weibo);
				}
				//删除指定id的微博，为避免重复推荐
				recommendDAO.deleteRcommendWeiboById(id,userId);
			}			
		}
		//返回推荐微博的list,如果没有内容则是一个空列表
		return wlist;
	}

	public void updateRecommendResult() {
		RecommendEngine re = new RecommendEngine();
		re.recommend();
	}

}
