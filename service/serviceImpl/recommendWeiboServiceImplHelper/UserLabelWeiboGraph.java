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
 * ������д���һ�� �û�-��ǩ-΢�� ��ͼ���ṩ�˸���ͼ���ݵĽӿ�
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
	 * ���޶�΢���ȶȵ�Ӱ���ۿ�
	 */
	final private double likeDiscount = 0.5;
	/**
	 * ���۶�΢���ȶȵ�Ӱ���ۿ�
	 */
	final private double commentDiscount = 0.8;
	/**
	 * ת����΢���ȶȵ�Ӱ���ۿ�
	 */
	final private double forwardDiscount = 1.0;
	/**
	 * ΢�����Ŷ���͵ģ����ڹ�һ�����Ŷ�
	 */
	private Double min = 99999999999.0;
	/**
	 * ΢�����Ŷ���ߵģ����ڹ�һ�����Ŷ�
	 */
	private Double max = -1.0;
	
	/**
	 * �û�-(��ǩ��Ȩ��)ӳ��
	 */
	private Map<String,List<Entry<String,Double>>> user_labelMap = null;
	/**
	 * ��ǩ-΢��ӳ��
	 */
	private Map<String,List<String>> label_weiboMap = null;
	/**
	 * ΢��-�ȶ�ӳ��
	 */
	private Map<String,Double> weibo_popMap = null;
	
	
	public UserLabelWeiboGraph(){
		user_labelMap = new HashMap<String,List<Entry<String,Double>>>();
		label_weiboMap = new HashMap<String,List<String>>();
		weibo_popMap = new HashMap<String,Double>();
		
		//�����΢��-��ǩ-�û�ͼ
		//updateUser();
		//updateWeibo();
	}
	
	/**
	 * �����ݿ���ȡ�������û�id
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
	 * Ϊ�û�����ͼ�еı�ǩ
	 * @param idҪ���±�ǩ���û�
	 */
	public void updateUser(String id){
		//���û�ӵ�еı�ǩ
		Map<String,Double> labels = recommendDAO.getUserLabel(id);
		if(labels == null){//������ݿⷵ�ؿգ���������
			System.out.println("updateUser(String id):���ݿ����recommendDAO.getUserLabel(id)����NULL");
			return;
		}
		//��ͼ���û������ı�ǩ
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
	 * ����ͼ�е�΢��
	 */
	public void updateWeibo(int num){
		List<String> wlist = weiboDAO.getTotalWeibo();
		
		if(wlist == null){//������ݿⷵ�ؿգ���������
			System.out.println("updateWeibo(int num):���ݿ����weiboDAO.getTotalWeibo()����NULL");
			return;
		}
		
		int i = 0;
		for(String wid : wlist){//��ǰnum��΢������ͼ��
			List<String> labelList = recommendDAO.getWeiboLabels(wid);
			if(labelList == null){
				System.out.println("updateWeibo(int num):���ݿ����recommendDAO.getWeiboLabels(wid)����NULL");
			}else{
				for(String label : labelList){//������΢�������б�ǩ
					updateWeibo(wid,label);//���¸�΢����ͼ�е���Ϣ
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
	 * ��ͼ�ж�Ӧ�ı�ǩ�²���/����΢��
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
	 * ���ô˽ӿڣ���������û����Ƽ��б�
	 * @param uid�û�id
	 * @return
	 */
	public List<String> RecommendWeibo(String uid){
		//Ϊ���û����ɳ�ʼ�Ƽ�
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
	 * �����Ƽ����ɴ�С�����Ƽ�����Խ��Խ��ǰ
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
