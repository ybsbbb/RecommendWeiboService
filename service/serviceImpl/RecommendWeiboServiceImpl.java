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
 * Î¢²©ÍÆ¼öµÄÊµÏÖÀà
 * @author yangbo
 *
 */
@Service("recommendWeiboService")
public class RecommendWeiboServiceImpl implements RecommendWeiboService{

	@Autowired
	WeiboDAO weiboDAO;//DAO²ã¶ÔÏó
	@Autowired
	RecommendDAO recommendDAO;//DAO²ã¶ÔÏó
	
	public List<Weibo> getRecommentWeiboList(String userId, int pageIndex, int numberPerPage) {
		//´ÓÊı¾İ¿âÖĞÈ¡³öÎª¸ÃÓÃ»§ÍÆ¼öµÄÇ°numberPerPageÌõÍÆ¼ö,
		//µÚ¶ş¸ö²ÎÊıÉèÖÃÎª0ÊÇÒòÎªÃ¿´Î¶¼»á°ÑÍÆ¼ö¹ıµÄÎ¢²©´ÓÍÆ¼öÁĞ±íÉ¾È¥£¬²»ÂÛÇëÇóµ½µÚ¼¸Ò³¶¼ÊÇ´ÓµÚÒ»Ò³È¡
		List<String> wids = recommendDAO.getRecommendWeiboList(userId, 0, numberPerPage);
		//´´½¨Òª·µ»ØµÄÍÆ¼öÎ¢²©ÁĞ±íwlist
		List<Weibo> wlist = new ArrayList<Weibo>();
		
		//Èç¹û·µ»ØµÄÍÆ¼öÁĞ±í²»Îª¿Õ£¬¸ù¾İÍÆ¼öÎ¢²©µÄidÈ¡µ½ÕæÕıµÄÎ¢²©£¬·ÅÈëwlist
		if(wids != null){
			for(String id : wids){
				Weibo weibo = weiboDAO.getWeibo(id);
				if(weibo != null){
					wlist.add(weibo);
				}
<<<<<<< HEAD
				//É¾³ıÖ¸¶¨idµÄÎ¢²©£¬Îª±ÜÃâÖØ¸´ÍÆ¼ö
				recommendDAO.deleteRcommendWeiboById(id,userId);
=======
				//åˆ é™¤æŒ‡å®šidçš„å¾®åšï¼Œä¸ºé¿å…é‡å¤æ¨è
				recommenddao.deleteRcommendWeiboById(id,userId);
>>>>>>> origin/master
			}			
		}
		//·µ»ØÍÆ¼öÎ¢²©µÄlist,Èç¹ûÃ»ÓĞÄÚÈİÔòÊÇÒ»¸ö¿ÕÁĞ±í
		return wlist;
	}

	public void updateRecommendResult() {
		RecommendEngine re = new RecommendEngine();
		re.recommend();
	}

}
