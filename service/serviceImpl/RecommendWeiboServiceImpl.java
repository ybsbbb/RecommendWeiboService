/**
 * 
 */
package cn.edu.bjtu.weibo.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.edu.bjtu.weibo.dao.RecommendDAO;
import cn.edu.bjtu.weibo.dao.WeiboDAO;
import cn.edu.bjtu.weibo.model.Weibo;
import cn.edu.bjtu.weibo.service.RecommendWeiboService;

/**
 * ΢���Ƽ���ʵ����
 * @author yangbo
 *
 */
@Component
public class RecommendWeiboServiceImpl implements RecommendWeiboService{

	@Autowired
	WeiboDAO weibodao;//DAO�����
	@Autowired
	RecommendDAO recommenddao;//DAO�����
	
	public List<Weibo> getRecommentWeiboList(String userId, int pageIndex, int numberPerPage) {
		//�����ݿ���ȡ��Ϊ���û��Ƽ���ǰnumberPerPage���Ƽ�,
		//�ڶ�����������Ϊ0����Ϊÿ�ζ�����Ƽ�����΢�����Ƽ��б�ɾȥ���������󵽵ڼ�ҳ���Ǵӵ�һҳȡ
		List<String> wids = recommenddao.getRecommendWeiboList(userId, 0, numberPerPage);
		//����Ҫ���ص��Ƽ�΢���б�wlist
		List<Weibo> wlist = new ArrayList<Weibo>();
		
		//������ص��Ƽ��б�Ϊ�գ������Ƽ�΢����idȡ��������΢��������wlist
		if(wids != null){
			for(String id : wids){
				Weibo weibo = weibodao.getWeibo(id);
				if(weibo != null){
					wlist.add(weibo);
				}
				//ɾ��ָ��id��΢����Ϊ�����ظ��Ƽ�
				recommenddao.deleteRecommendWeiboById(id,userId);
			}			
		}
		//�����Ƽ�΢����list,���û����������һ�����б�
		return wlist;
	}

}
