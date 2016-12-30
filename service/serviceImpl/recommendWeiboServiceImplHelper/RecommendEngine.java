/**
 * 
 */
package cn.edu.bjtu.weibo.service.serviceImpl.recommendWeiboServiceHelper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cn.edu.bjtu.weibo.dao.RecommendDAO;
import cn.edu.bjtu.weibo.dao.UserDAO;

/**
 * ��������࣬����<code>recommend()</code>��<code>recommend(int num)</code>�������ɽ��Ƽ����д�����ݿ�<br>
 * ��Ҫÿ��һ��ʱ�����һ���������û����Ƽ��б�����10����
 * @author yangbo
 *
 */
public class RecommendEngine {
	
	/**
	 * �Ƽ��㷨�߼������ֵĶ���
	 */
	UserLabelWeiboGraph graph;
	/**
	 * �����Ƽ�����Ķ���
	 */
	RecommendListFilter filter;
	/**
	 * ����ע���û�
	 */
	List<String> allusers;
	
	@Autowired
	UserDAO userDAO;
	@Autowired
	RecommendDAO recommendDAO;

	/**
	 * ����������ʼ���㷨�͹�����
	 */
	public RecommendEngine(){
		graph = new UserLabelWeiboGraph();
		filter = new RecommendListFilter();
	}
	
	/**
	 * ʹ���Ƽ��㷨����Ƽ�
	 * @param num ��ѡ�Ƽ�����
	 */
	public void recommend(int num){
		allusers = userDAO.getTotalUserId();//ȡ�����û�
		if(allusers != null){//����ֵ��Ϊ���������Ƽ��㷨����������
			for(String u : allusers){//�������û�����ͼ��
				graph.updateUser(u);
			}
			graph.updateWeibo(num);	//�����ݿ�ǰnum������ȡ������ͼ��
			for(String u : allusers){//���������û����Ƽ��б���д�����ݿ�
				List<String> originalList = graph.RecommendWeibo(u);//����ָ���û��ĳ�ʼ�Ƽ��б�
				List<String> filteredList = filter.getFilteredRecommendList(u, originalList);//�Գ�ʼ�Ƽ��б���й���
				this.insertIntoDb(u, filteredList);//�������Ƽ��б�д�����ݿ�
			}
			return;
		}
		System.out.println("recommend(int num)�����ݿ����userDAO.getTotalUserId()���ؿ�");
	}
	/**
	 * ʹ���Ƽ��㷨����Ƽ�����ѡ����Ϊ100��
	 */
	public void recommend(){
		allusers = userDAO.getTotalUserId();
		if(allusers != null){
			for(String u : allusers){
				graph.updateUser(u);
			}
			graph.updateWeibo(100);//Ĭ������
			for(String u : allusers){
				List<String> originalList = graph.RecommendWeibo(u);
				List<String> filteredList = filter.getFilteredRecommendList(u, originalList);
				this.insertIntoDb(u, filteredList);
			}
			return;
		}
		System.out.println("recommend()�����ݿ����userDAO.getTotalUserId()���ؿ�");
	}
	
	/**
	 * �������Ƽ����д�����ݿ�
	 * @param uid
	 * @param wlist
	 */
	private void insertIntoDb(String uid,List<String> wlist){
		boolean resOfDel = recommendDAO.deleteRecommendWeibo(uid);//���Ƽ��б�ɾ��
		if(resOfDel){
			System.out.println("ɾ���û�"+ uid +"���Ƽ��б�ɹ�");
		}else{
			System.out.println("ɾ���û�"+ uid +"���Ƽ��б�ʧ��");
		}
		boolean resOfSet = recommendDAO.setRecommendWeiboList(uid, wlist);//���Ƽ��б�д��
		if(resOfSet){
			System.out.println("�����û�"+ uid +"���Ƽ��б�ɹ�");
		}else{
			System.out.println("�����û�"+ uid +"���Ƽ��б�ʧ��");
		}
	}
}
