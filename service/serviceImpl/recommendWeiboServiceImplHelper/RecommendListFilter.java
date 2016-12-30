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
 * 这个类用来为推荐的微博进行过滤
 * @author 杨博
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
	 * 构造器
	 */
	public RecommendListFilter(){
	}
	
	/**
	 * 将用户的推荐列表进行过滤，除去用户可以在自己首页看到的内容
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
			//黑名单和屏蔽没有在接口里找到，暂不考虑
		}
		for(String s : delSet){
			originalList.remove(s);
		}
		return originalList;
	}

	/**
	 * 判断这条微博是否@该用户
	 * @param wid
	 * @param uid
	 * @return
	 */
	private boolean isWeiboAtUser(String wid, String uid) {
		List<String> atlist = weiboDAO.getAtUserList(wid);
		if(atlist == null){//数据库返回null，表明没有@任何人
			return false;
		}
		if(atlist.contains(uid)){//@列表包含该用户
			return true;
		}
		return false;
	}

	/**
	 * 判断用户是否阅读过本条微博，根据用户是否点赞，评论，转发过
	 * @param wid
	 * @param uid
	 * @return
	 */
	private boolean hasUserRead(String wid, String uid) {
		/**
		 * 该用户有没有为这条微博点过赞
		 */
		int likenumber = weiboDAO.getLikeNumber(wid);
		if(likenumber != 0){
			List<String> likelist = weiboDAO.getLikeList(wid, 0, likenumber);
			if(likelist == null){//点赞人列表返回空
				//System.out.println("hasUserRead(String wid, String uid):数据库操作weiboDAO.getLikeList(wid, 0, likenumber)返回空");
			}else{
				if(likelist.contains(uid)){
					return true;
				}
			}					
		}
		
		/**
		 * 该用户有没有为这条微博评论
		 */
		int commetnumber = weiboDAO.getCommentNumber(wid);
		if(commetnumber != 0){
			List<String> commentlist = weiboDAO.getCommentList(wid, 0, commetnumber);
			if(commentlist == null){//评论人列表返回空
				//System.out.println("hasUserRead(String wid, String uid):数据库操作weiboDAO.getCommentList(wid, 0, likenumber)返回空");
			} else{
				for(String cid : commentlist){
					String owner = commentDAO.getOwner(cid);
					if(owner != null){
						if(owner.equals(uid)){
							return true;
						}
					} else{
						//System.out.println("hasUserRead(String wid, String uid):数据库操作commentDAO.getOwner(cid)返回空");
					}
				}
			}
		}	
		
		/**
		 * 该用户有没有转发本条微博
		 */
		int forwardnumber = weiboDAO.getForwardNumber(wid);
		if(forwardnumber == 0){//转发人数为0，可以直接返回false，表明用户未阅读该微博
			return false;
		}
		List<String> forwardlist = weiboDAO.getForwardList(wid, 0, forwardnumber);
		if(forwardlist == null){//转发人列表返回空
			//System.out.println("hasUserRead(String wid, String uid):数据库操作weiboDAO.getForwardList(wid, 0, likenumber)返回空");
			return false;
		}
		for(String fid : forwardlist){
			String owner = weiboDAO.getOwner(fid);
			if(owner != null){
				if(owner.equals(uid)){
					return true;
				}				
			}else{
				//System.out.println("hasUserRead(String wid, String uid):数据库操作commentDAO.getOwner(fid)返回空");
			}
		}
		return false;
	}

	/**
	 * 判断这位用户与推荐的微博的博主是否相识(本人/关注)
	 * @param wid
	 * @param uid
	 * @return
	 */
	private boolean isUserKnowOwner(String wid, String uid) {
		String owner = weiboDAO.getOwner(wid);
		if(owner == null){
			//System.out.println("hasUserRead(String wid, String uid):数据库操作weiboDAO.getOwner(wid)返回空");
			return false;
		}
		if(owner.equals(uid)){//表明这条微博是他本人发的
			return true;
		}
		int followingnumber = userDAO.getFollowingNumber(uid);
		List<String> ulist = userDAO.getFollowing(uid, 0, followingnumber);//此人关注列表
		if(ulist == null){
			return false;
		}
		if(ulist.contains(owner)){
			return true;
		}
		return false;
	}
}
