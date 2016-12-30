/**
 * 
 */
package cn.edu.bjtu.weibo.service.serviceImpl.recommendWeiboServiceHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import cn.edu.bjtu.weibo.dao.CommentDAO;
import cn.edu.bjtu.weibo.dao.UserDAO;
import cn.edu.bjtu.weibo.dao.WeiboDAO;

/**
 * ���������Ϊ�Ƽ���΢�����й���
 * @author �
 *
 */
public class RecommendListFilter {
	
	@Autowired
	UserDAO userDAO;
	@Autowired
	WeiboDAO weiboDAO;
	@Autowired
	CommentDAO commentDAO;
	/**
	 * ������
	 */
	public RecommendListFilter(){
	}
	
	/**
	 * ���û����Ƽ��б���й��ˣ���ȥ�û��������Լ���ҳ����������
	 * @param uid
	 * @return
	 */
	public List<String> getFilteredRecommendList(String uid, List<String> originalList){
		Set<String> delSet = new HashSet<String>();
		for(String wid : originalList){
			if(isUserKnowOwner(wid,uid)){
				//originalList.remove(wid);
				delSet.add(wid);
			} else if(hasUserRead(wid,uid)){
				//originalList.remove(wid);
				delSet.add(wid);
			} else if(isWeiboAtUser(wid,uid)){
				//originalList.remove(wid);
				delSet.add(wid);
			}
			//������������û���ڽӿ����ҵ����ݲ�����
		}
		for(String s : delSet){
			originalList.remove(s);
		}
		return originalList;
	}

	/**
	 * �ж�����΢���Ƿ�@���û�
	 * @param wid
	 * @param uid
	 * @return
	 */
	private boolean isWeiboAtUser(String wid, String uid) {
		List<String> atlist = weiboDAO.getAtUserList(wid);
		if(atlist == null){//���ݿⷵ��null������û��@�κ���
			return false;
		}
		if(atlist.contains(uid)){//@�б�������û�
			return true;
		}
		return false;
	}

	/**
	 * �ж��û��Ƿ��Ķ�������΢���������û��Ƿ���ޣ����ۣ�ת����
	 * @param wid
	 * @param uid
	 * @return
	 */
	private boolean hasUserRead(String wid, String uid) {
		/**
		 * ���û���û��Ϊ����΢�������
		 */
		int likenumber = weiboDAO.getLikeNumber(wid);
		if(likenumber != 0){
			List<String> likelist = weiboDAO.getLikeList(wid, 0, likenumber);
			if(likelist == null){//�������б��ؿ�
				//System.out.println("hasUserRead(String wid, String uid):���ݿ����weiboDAO.getLikeList(wid, 0, likenumber)���ؿ�");
			}else{
				if(likelist.contains(uid)){
					return true;
				}
			}					
		}
		
		/**
		 * ���û���û��Ϊ����΢������
		 */
		int commetnumber = weiboDAO.getCommentNumber(wid);
		if(commetnumber != 0){
			List<String> commentlist = weiboDAO.getCommentList(wid, 0, commetnumber);
			if(commentlist == null){//�������б��ؿ�
				//System.out.println("hasUserRead(String wid, String uid):���ݿ����weiboDAO.getCommentList(wid, 0, likenumber)���ؿ�");
			} else{
				for(String cid : commentlist){
					String owner = commentDAO.getOwner(cid);
					if(owner != null){
						if(owner.equals(uid)){
							return true;
						}
					} else{
						//System.out.println("hasUserRead(String wid, String uid):���ݿ����commentDAO.getOwner(cid)���ؿ�");
					}
				}
			}
		}	
		
		/**
		 * ���û���û��ת������΢��
		 */
		int forwardnumber = weiboDAO.getForwardNumber(wid);
		if(forwardnumber == 0){//ת������Ϊ0������ֱ�ӷ���false�������û�δ�Ķ���΢��
			return false;
		}
		List<String> forwardlist = weiboDAO.getForwardList(wid, 0, forwardnumber);
		if(forwardlist == null){//ת�����б��ؿ�
			//System.out.println("hasUserRead(String wid, String uid):���ݿ����weiboDAO.getForwardList(wid, 0, likenumber)���ؿ�");
			return false;
		}
		for(String fid : forwardlist){
			String owner = weiboDAO.getOwner(fid);
			if(owner != null){
				if(owner.equals(uid)){
					return true;
				}				
			}else{
				//System.out.println("hasUserRead(String wid, String uid):���ݿ����commentDAO.getOwner(fid)���ؿ�");
			}
		}
		return false;
	}

	/**
	 * �ж���λ�û����Ƽ���΢���Ĳ����Ƿ���ʶ(����/��ע)
	 * @param wid
	 * @param uid
	 * @return
	 */
	private boolean isUserKnowOwner(String wid, String uid) {
		String owner = weiboDAO.getOwner(wid);
		if(owner == null){
			//System.out.println("hasUserRead(String wid, String uid):���ݿ����weiboDAO.getOwner(wid)���ؿ�");
			return false;
		}
		if(owner.equals(uid)){//��������΢���������˷���
			return true;
		}
		int followingnumber = userDAO.getFollowingNumber(uid);
		List<String> ulist = userDAO.getFollowing(uid, 0, followingnumber);//���˹�ע�б�
		if(ulist == null){
			return false;
		}
		if(ulist.contains(owner)){
			return true;
		}
		return false;
	}
}
