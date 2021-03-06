<<<<<<< HEAD:pisces/src/main/java/com/yanzi/pisces/service/UserCollegeService.java
package com.yanzi.pisces.service;

import java.util.List;




import com.yanzi.common.entity.college.course.CourseInfo;
import com.yanzi.common.entity.user.BillsInfo;
import com.yanzi.common.service.CUserCollegeService;
import com.yanzi.pisces.entity.CourseTermInfo;
import com.yanzi.pisces.entity.UserLessonStatus;
import com.yanzi.pisces.entity.UserRank;
import com.yanzi.pisces.entity.UserTermCourseEntity;
import com.yanzi.pisces.entity.UserCollegeStatus;
import com.yanzi.pisces.entity.UserCourseTermStatus;

public interface UserCollegeService extends CUserCollegeService {
    /**
     * 获取用户CourseId
     * 
     * @param userId
     * @return
     */
    public List<Long> loadCourseIdList(long userId);

    /**
     * 用户完成关卡
     * 
     * @param userId
     * @param courseId
     * @param lessonId
     * @param lessonKnowledge
     * @param exp
     * @return 新增的exp
     */
    long completeLesson(long userId, long courseId, long lessonId, long lessonKnowledge,long exp);

    /**
     * 获取用户课程期的状态
     * 
     * @param userId
     * @param courseId
     * @param termId
     * @return
     */
    UserCourseTermStatus loadCourseTermStatus(long userId, long courseId, long termId);

    /**
     * 获取用户college的状态
     * 
     * @param userId
     * @return
     */
    UserCollegeStatus loadStatus(long userId);

    /**
     * 获取用户lesson状态
     * 
     * @param userId
     * @param courseId
     * @param termId
     * @param lessonId
     * @return
     */
    UserLessonStatus loadLessonStatus(long userId, long courseId, long termId, long lessonId);

    int loadRankValue(long userId);

    int loadCourseTermRankValue(long userId, long courseId, long termId);

    List<UserRank> loadRankList(long userId);

    List<UserRank> loadCourseTermRankList(long userId, long courseId, long termId);

    List<UserRank> loadWeekRankList(long userId);

    List<UserRank> loadCourseTermWeekRankList(long userId, long courseId, long termId);

    /**
     * 用户购买课程
     * @param userId
     * @param termId
     * @author dusc
     */
    void userPurchaseTerm(long userId,long courseId,long termId,double coins);
    /**
     * 获取全部课程
     * @return
     */
    public List<CourseInfo> getAllCourseInfo();

	    /**获取用户购买的课程Id
     * 
     * @return
     */
    List<UserTermCourseEntity> selectUserCourseTermByUserId(Long userId);
    
    public void userPurchase(long userId,long courseId,long termId,double coins);
    
    public List<BillsInfo> checkPurchase(long userId,long courseId,long termId);
    
    public int loadCourseTermRank(long userId,long courseId,long termId,List<UserRank> userRanks);
    

    public List<Boolean> loadCourseTermWeekCompleteStatus(long userId, long courseId, long termId);

    /**
     * 获取课程下的购买用户
     * @return
     */
    public List<Long> getUserId(long courseId,long termId);


    public boolean checkFriend(long userId,long friendId);
    
    public long getCourseIdByTermId(long termId);

	public List<UserRank> loadFCourseTermRankList(long userId, long courseId, long termId);


}
=======
package com.yanzi.pisces.service;

import java.util.List;




import com.yanzi.common.entity.college.course.CourseInfo;
import com.yanzi.common.entity.user.BillsInfo;
import com.yanzi.common.service.CUserCollegeService;
import com.yanzi.pisces.entity.CourseTermInfo;
import com.yanzi.pisces.entity.UserLessonStatus;
import com.yanzi.pisces.entity.UserRank;
import com.yanzi.pisces.entity.UserTermCourseEntity;
import com.yanzi.pisces.entity.UserCollegeStatus;
import com.yanzi.pisces.entity.UserCourseTermStatus;

public interface UserCollegeService extends CUserCollegeService {
    /**
     * 获取用户CourseId
     * 
     * @param userId
     * @return
     */
    public List<Long> loadCourseIdList(long userId);

    /**
     * 用户完成关卡
     * 
     * @param userId
     * @param courseId
     * @param lessonId
     * @param lessonKnowledge
     * @param exp
     * @return 新增的exp
     */
    long completeLesson(long userId, long courseId, long lessonId, long lessonKnowledge,long exp);

    /**
     * 获取用户课程期的状态
     * 
     * @param userId
     * @param courseId
     * @param termId
     * @return
     */
    UserCourseTermStatus loadCourseTermStatus(long userId, long courseId, long termId);

    /**
     * 获取用户college的状态
     * 
     * @param userId
     * @return
     */
    UserCollegeStatus loadStatus(long userId);

    /**
     * 获取用户lesson状态
     * 
     * @param userId
     * @param courseId
     * @param termId
     * @param lessonId
     * @return
     */
    UserLessonStatus loadLessonStatus(long userId, long courseId, long termId, long lessonId);

    int loadRankValue(long userId);

    int loadCourseTermRankValue(long userId, long courseId, long termId);

    List<UserRank> loadRankList(long userId);

    List<UserRank> loadCourseTermRankList(long userId, long courseId, long termId);

    List<UserRank> loadWeekRankList(long userId);

    List<UserRank> loadCourseTermWeekRankList(long userId, long courseId, long termId);

    /**
     * 用户购买课程
     * @param userId
     * @param termId
     * @author dusc
     */
    void userPurchaseTerm(long userId,long courseId,long termId,double coins);
    /**
     * 获取全部课程
     * @return
     */
    public List<CourseInfo> getAllCourseInfo();

	    /**获取用户购买的课程Id
     * 
     * @return
     */
    List<UserTermCourseEntity> selectUserCourseTermByUserId(Long userId);
    
    public void userPurchase(long userId,long courseId,long termId,double coins);
    
    public List<BillsInfo> checkPurchase(long userId,long courseId,long termId);
    
    public int loadCourseTermRank(long userId,long courseId,long termId,List<UserRank> userRanks);
    

    public List<Boolean> loadCourseTermWeekCompleteStatus(long userId, long courseId, long termId);

    /**
     * 获取课程下的购买用户
     * @return
     */
    public List<Long> getUserId(long courseId,long termId);


    public boolean checkFriend(long userId,long friendId);
    
    public long getCourseIdByTermId(long termId);


}
>>>>>>> refs/remotes/origin/master:api/pisces/src/main/java/com/yanzi/pisces/service/UserCollegeService.java
