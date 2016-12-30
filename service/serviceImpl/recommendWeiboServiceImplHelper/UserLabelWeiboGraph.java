/**
 * 
 */
package cn.edu.bjtu.weibo.service.serviceImpl.recommendWeiboServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import cn.edu.bjtu.weibo.dao.RecommendDAO;
import cn.edu.bjtu.weibo.dao.UserDAO;
import cn.edu.bjtu.weibo.dao.WeiboDAO;

/**
 * 这个类中存了一张 用户-标签-微博 的图，提供了更新图内容的接口
 * @author yangbo
 *
 */
public class UserLabelWeiboGraph {
	
	@Autowired
	RecommendDAO recommendDAO;
	@Autowired
	UserDAO userDAO;
	@Autowired
	WeiboDAO weiboDAO;
	
	
	
	/**
	 * 点赞对微博热度的影响折扣
	 */
	final private double likeDiscount = 0.5;
	/**
	 * 评论对微博热度的影响折扣
	 */
	final private double commentDiscount = 0.8;
	/**
	 * 转发对微博热度的影响折扣
	 */
	final private double forwardDiscount = 1.0;
	/**
	 * 微博热门度最低的，用于归一化热门度
	 */
	private Double min = 99999999999.0;
	/**
	 * 微博热门度最高的，用于归一化热门度
	 */
	private Double max = -1.0;
	
	/**
	 * 用户-(标签，权重)映射
	 */
	private Map<String,List<Entry<String,Double>>> user_labelMap = null;
	/**
	 * 标签-微博映射
	 */
	private Map<String,List<String>> label_weiboMap = null;
	/**
	 * 微博-热度映射
	 */
	private Map<String,Double> weibo_popMap = null;
	
	
	public UserLabelWeiboGraph(){
		user_labelMap = new HashMap<String,List<Entry<String,Double>>>();
		label_weiboMap = new HashMap<String,List<String>>();
		weibo_popMap = new HashMap<String,Double>();
		
		//构造出微博-标签-用户图
		//updateUser();
		//updateWeibo();
	}
	
	/**
	 * 从数据库中取出所有用户id
	 * @param ulist
	 */
	/*private void updateUser(){
		List<String> ulist = userdao.getTotalUserId();
		for(String u : ulist){
			updateUser(u);
		}
	}
	*/
	/**
	 * 为用户更新图中的标签
	 * @param id要更新标签的用户
	 */
	public void updateUser(String id){
		//该用户拥有的标签
		Map<String,Double> labels = recommendDAO.getUserLabel(id);
		if(labels == null){//如果数据库返回空，不做操作
			System.out.println("updateUser(String id):数据库操作recommendDAO.getUserLabel(id)返回NULL");
			return;
		}
		//在图中用户关联的标签
		List<Entry<String,Double>> ulist = user_labelMap.get(id);
		if(ulist == null){
			ulist = new ArrayList<Entry<String,Double>>();
			ulist.addAll(labels.entrySet());
			user_labelMap.put(id, ulist);
		} else {
			ulist = new ArrayList<Entry<String,Double>>();
			ulist.addAll(labels.entrySet());
			user_labelMap.replace(id, ulist);
		}
	}
	
	/**
	 * 更新图中的微博
	 */
	public void updateWeibo(int num){
		List<String> wlist = weiboDAO.getTotalWeibo();
		
		if(wlist == null){//如果数据库返回空，不做处理
			System.out.println("updateWeibo(int num):数据库操作weiboDAO.getTotalWeibo()返回NULL");
			return;
		}
		
		int i = 0;
		for(String wid : wlist){//将前num条微博插入图中
			List<String> labelList = recommendDAO.getWeiboLabels(wid);
			if(labelList == null){
				System.out.println("updateWeibo(int num):数据库操作recommendDAO.getWeiboLabels(wid)返回NULL");
			}else{
				for(String label : labelList){//遍历该微博的所有标签
					updateWeibo(wid,label);//更新该微博在图中的信息
				}
				i++;
				if(i > num){
					break;
				}				
			}
		}
		for(Entry<String,Double> e : weibo_popMap.entrySet()){
			Double pop = (e.getValue() - min) / (max - min);
			weibo_popMap.replace(e.getKey(), pop);
		}
	}
	
	/**
	 * 在图中对应的标签下插入/更新微博
	 * @param wid
	 * @param label
	 */
	private void updateWeibo(String wid,String label){
		int forward = weiboDAO.getForwardNumber(wid);
		int like = weiboDAO.getLikeNumber(wid);
		int comment = weiboDAO.getCommentNumber(wid);
		Double pop = weibo_popMap.get(wid);
		
		if(pop == null){
			pop = forward * this.forwardDiscount +
					like * this.likeDiscount + 
					comment * this.commentDiscount;
			if(pop>max){
				max = pop;
			}
			if(pop<min){
				min = pop;
			}
			weibo_popMap.put(wid, pop);
		}
		
		List<String> list = label_weiboMap.get(label);
		if(list == null){
			list = new ArrayList<String>();
			label_weiboMap.put(label, list);
			list.add(wid);
		} else {
			list.add(wid);
		}
	}
	
	/**
	 * 调用此接口，返回这个用户的推荐列表
	 * @param uid用户id
	 * @return
	 */
	public List<String> RecommendWeibo(String uid){
		//为该用户生成初始推荐
		List<Entry<String,Double>> ulist = user_labelMap.get(uid);
		Map<String,Double> recommendMap = new HashMap<String,Double>();
		for(Entry<String,Double> item : ulist){
			String label = item.getKey();
			Double weight = item.getValue();
			List<String> wlist = label_weiboMap.get(label);
			for(String wid : wlist){
				Double oldpro = recommendMap.get(wid);
				Double newpro = weight * weibo_popMap.get(wid);
				if(oldpro == null){
					recommendMap.put(wid, newpro);
				} else {
					recommendMap.replace(wid, oldpro + newpro);
				}
			}
		}
		List<String> sortedList = sortWeiboByProbability(recommendMap);
		return sortedList;
	}
	
	/**
	 * 根据推荐理由大小排序，推荐理由越大越靠前
	 * @param weibo_probabilityMap
	 * @return
	 */
	private List<String> sortWeiboByProbability(Map<String, Double> weibo_probabilityMap) {
		List<Entry<String,Double>> list = new ArrayList<Entry<String,Double>>(weibo_probabilityMap.entrySet());
		Collections.sort(list, new Comparator<Entry<String,Double>>(){
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if(o2.getValue() - o1.getValue() > 0){
					return 1;
				} else if(o2.getValue() - o1.getValue() == 0){
					return 0;
				}
				return -1;
			}
		});
		List<String> weiboList = new ArrayList<String>();
		for(Entry<String,Double> item : list){
			weiboList.add(item.getKey());
		}
		return weiboList;
	}
}
