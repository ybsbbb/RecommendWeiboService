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
 * ΢���Ƽ���ʵ����
 * @author yangbo
 *
 */
@Service("recommendWeiboService")
public class RecommendWeiboServiceImpl implements RecommendWeiboService{

	@Autowired
	WeiboDAO weiboDAO;//DAO�����
	@Autowired
	RecommendDAO recommendDAO;//DAO�����
	
	public List<Weibo> getRecommentWeiboList(String userId, int pageIndex, int numberPerPage) {
		//�����ݿ���ȡ��Ϊ���û��Ƽ���ǰnumberPerPage���Ƽ�,
		//�ڶ�����������Ϊ0����Ϊÿ�ζ�����Ƽ�����΢�����Ƽ��б�ɾȥ���������󵽵ڼ�ҳ���Ǵӵ�һҳȡ
		List<String> wids = recommendDAO.getRecommendWeiboList(userId, 0, numberPerPage);
		//����Ҫ���ص��Ƽ�΢���б�wlist
		List<Weibo> wlist = new ArrayList<Weibo>();
		
		//������ص��Ƽ��б�Ϊ�գ������Ƽ�΢����idȡ��������΢��������wlist
		if(wids != null){
			for(String id : wids){
				Weibo weibo = weiboDAO.getWeibo(id);
				if(weibo != null){
					wlist.add(weibo);
				}
<<<<<<< HEAD
				//ɾ��ָ��id��΢����Ϊ�����ظ��Ƽ�
				recommendDAO.deleteRcommendWeiboById(id,userId);
=======
				//删除指定id的微博，为避免重复推荐
				recommenddao.deleteRcommendWeiboById(id,userId);
>>>>>>> origin/master
			}			
		}
		//�����Ƽ�΢����list,���û����������һ�����б�
		return wlist;
	}

	public void updateRecommendResult() {
		RecommendEngine re = new RecommendEngine();
		re.recommend();
	}

}
