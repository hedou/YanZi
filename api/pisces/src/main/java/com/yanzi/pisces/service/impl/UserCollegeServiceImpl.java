package com.yanzi.pisces.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yanzi.common.entity.Date;
import com.yanzi.common.entity.college.course.CourseInfo;
import com.yanzi.common.entity.college.level.LevelInfo;
import com.yanzi.common.entity.user.BillsInfo;
import com.yanzi.common.entity.user.UserInfo;
import com.yanzi.common.service.CUserFriendService;
import com.yanzi.common.service.impl.CUserCollegeServiceImpl;
import com.yanzi.common.utils.TimeUtils;
import com.yanzi.pisces.data.LessonData;
import com.yanzi.pisces.data.LevelData;
import com.yanzi.pisces.data.TermData;
import com.yanzi.pisces.entity.CourseTermInfo;
import com.yanzi.pisces.entity.RankInfo;
import com.yanzi.pisces.entity.UserCollegeStatus;
import com.yanzi.pisces.entity.UserCourseTermStatus;
import com.yanzi.pisces.entity.UserFriendInfo;
import com.yanzi.pisces.entity.UserLessonStatus;
import com.yanzi.pisces.entity.UserRank;
import com.yanzi.pisces.entity.UserTermCourseEntity;
import com.yanzi.pisces.entity.comparator.RankEntityCompartor;
import com.yanzi.pisces.mysql.CourseMapper;
import com.yanzi.pisces.mysql.LevelMapper;
import com.yanzi.pisces.mysql.UserCourseTermMapper;
import com.yanzi.pisces.service.UserCollegeService;
import com.yanzi.pisces.service.UserService;

@Service
public class UserCollegeServiceImpl extends CUserCollegeServiceImpl implements UserCollegeService {

    private static final int BASE_KNOWLEDGE_EXP = 2;
    private static final int DAY_COMPLETE_EXP = 3;
    private static final int SEVEN_DAY_COMPLETE_EXP = 5;
    // private static final int SEVEN_DAY = 7;
    private static final int DAY_COMPLETE_KNOWLEDGE = 10;

    @Autowired
    private UserCourseTermMapper userCourseTermMapper;
    @Autowired
    private TermData termData;
    @Autowired
    private LessonData lessonData;
    @Autowired
    private LevelData levelData;
    @Autowired
    private CUserFriendService cUserFriendService;
    @Autowired
    private UserService userService;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private LevelMapper levelMapper;
    @Autowired
    private UserCollegeService userCollegeService;

    @Override
    public List<Long> loadCourseIdList(long userId) {
        List<UserTermCourseEntity> userCourseTermList = userCourseTermMapper
                .selectUserCourseTermByUserId(userId);
        List<Long> userCourseIdList = new ArrayList<>();
        for (UserTermCourseEntity entity : userCourseTermList) {//courseTerm表匹配项 根据课程下term的valid参数筛选
            if (termData.isValid(entity.getTermId())) {
                userCourseIdList.add(entity.getCourseId());
            }
        }
        return userCourseIdList;
    }

    @Override
    public long completeLesson(long userId, long courseId, long lessonId, long lessonKnowledge,long exp) {
        long termId = this.loadCourseTermId(userId, courseId);
        long lessonMaxKnowledge = this.loadCourseTermLessonMaxKnowledge(userId, courseId, termId,
                lessonId);
        
        
        long newKnowledge = lessonKnowledge - lessonMaxKnowledge;
        Date date = TimeUtils.getDate();
        // update lesson knowledge
        this.saveCourseTermLessonKnowledge(userId, courseId, termId, lessonId, lessonKnowledge);
        if (newKnowledge <= 0) {
            return 0;
        }
        // update lesson max knowledge
        this.saveCourseTermLessonMaxKnowledge(userId, courseId, termId, lessonId, lessonKnowledge);
        // update course knowledge
        long courseKnowledge = this.loadCourseTermKnowledge(userId, courseId, termId);
        courseKnowledge += newKnowledge;
        this.saveCourseTermKnowledge(userId, courseId, termId, courseKnowledge);
        // update course term day knowledge
        long todayKnowledge = this.loadCourseTermDayKnowledge(userId, courseId, termId,
                date.getDay());
        todayKnowledge += newKnowledge;
        this.saveCourseTermDayKnowledge(userId, courseId, termId, todayKnowledge, date.getDay());
        // update user knowledge//更新总知识点
        long userKnowledge = this.loadKnowledge(userId);
        userKnowledge += newKnowledge;
        this.saveKnowledge(userId, userKnowledge);

        long newExp = exp;//newKnowledge * BASE_KNOWLEDGE_EXP;   dusc
        // complete day
        if (!this.courseTermDayIsComplete(userId, courseId, termId, date.getDay())) {
            if (todayKnowledge >= DAY_COMPLETE_KNOWLEDGE) {//更新今日目标完成状态
                this.courseTermDayComplete(userId, courseId, termId, date.getDay());//redis更新期完成状态
                long completeDayCount = this.loadCourseTermCompleteDayCount(userId, courseId,
                        termId);//坚持天数
                ++completeDayCount;
                this.saveCourseTermCompleteDayCount(userId, courseId, termId, completeDayCount);
                // raise exp TODO judge
                // if (completeDayCount % SEVEN_DAY == 0) {
                // newExp += SEVEN_DAY_COMPLETE_EXP;
                // } else {
                newExp += DAY_COMPLETE_EXP;//今日任务经验值增加
                // }
            }
        }
        // update user Exp//更新总经验值
        long userExp = this.loadExp(userId);
        userExp += newExp;
        this.saveExp(userId, userExp);
        // update course term exp
        long courseExp = this.loadCourseTermExp(userId, courseId, termId);
        courseExp += newExp;
        this.saveCourseTermExp(userId, courseId, termId, courseExp);
        // update user week exp
        long userWeekExp = this.loadWeekExp(userId, date.getWeek());
        userWeekExp += newExp;
        this.saveWeekExp(userId, date.getWeek(), userWeekExp);
        // update user course term week exp
        long courseWeekExp = this.loadCourseTermWeekExp(userId, courseId, termId, date.getWeek());
        courseWeekExp += newExp;
        this.saveCourseTermWeekExp(userId, courseId, termId, date.getWeek(), courseWeekExp);
        
        //更新等级状态
        long levelId = this.loadCourseTermLevel(userId, courseId, termId);//用户当前等级
        LevelInfo level = levelData.getByCourseIdAndExp(courseId, courseExp);//当前课程经验值更新后对应的等级
        double coins = 0;
        if(level.getId() != levelId){//升级，增加雁币
        	this.saveCourseTermLevel(userId, courseId, termId, level.getId());
        	coins += level.getCoin();
        }
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek == 1){//本周全部完成目标，增加雁币
        	List<Boolean> list = this.loadCourseTermWeekCompleteStatus(userId,courseId,termId);
        	boolean flag = true;
        	for(int i=0;i<list.size();i++){
        		if(!list.get(i)){
        			flag = false;
        			break;
        		}
        	}
        	if(flag){
        		coins += SEVEN_DAY_COMPLETE_EXP;
        	}
        }
        if(coins != 0){
        	UserInfo user = userService.loadUserInfo(userId);
        	user.setCoins(coins);
        	userService.saveUserInfo(user);//增加雁币
        	userCourseTermMapper.addCoins(userId, level.getCoin());
        }
        return newExp;
    }

    public List<Boolean> loadCourseTermWeekCompleteStatus(long userId, long courseId, long termId) {
        List<Boolean> weekCompleteStatus = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        for (int i = 0; i < dayOfWeek; ++i) {
            cal.add(Calendar.DATE, -(dayOfWeek - i - 1));
            String day = dateFormat.format(cal.getTime());
            boolean isComplete = this.courseTermDayIsComplete(userId, courseId, termId, day);
            weekCompleteStatus.add(isComplete);
            cal.add(Calendar.DATE, dayOfWeek - i - 1);
        }
        return weekCompleteStatus;
    }

    @Override
    public UserCourseTermStatus loadCourseTermStatus(long userId, long courseId, long termId) {
        UserCourseTermStatus status = new UserCourseTermStatus();
        
        Date date = TimeUtils.getDate();
        long exp = this.loadCourseTermExp(userId, courseId, termId);//经验
        status.setExp(exp);
        
        long totalKnowledge = this.loadCourseTermKnowledge(userId, courseId, termId);//知识点
        status.setTotalKnowledge(totalKnowledge);
        
        long dayKnowledge = this.loadCourseTermDayKnowledge(userId, courseId, termId,
                date.getDay());//每日知识点数
        status.setDayKnowledge(dayKnowledge);
        
        
        long sCompleteDayCount = this.loadCourseTermDayComplete(userId, courseId, termId);
        status.setSustainedCompleteDayCount(sCompleteDayCount);
        
        // week complete
        List<Boolean> weekCompleteStatus = this.loadCourseTermWeekCompleteStatus(userId, courseId,
                termId);
        status.setWeekCompleteStatus(weekCompleteStatus);
        
        // get rank
        int rank = this.loadCourseTermRankValue(userId, courseId, termId);
        status.setRank(rank);
        
        // get level
        long levelId = this.loadCourseTermLevel(userId, courseId, termId);
        LevelInfo level = levelData.get(levelId);
        status.setLevel(level);
        
        return status;
    }

    @Override
    public UserCollegeStatus loadStatus(long userId) {
        UserCollegeStatus status = new UserCollegeStatus();
        long exp = this.loadExp(userId);
        status.setExp(exp);
        long knowledge = this.loadKnowledge(userId);
        status.setKnowledge(knowledge);
        int rank = this.loadRankValue(userId);
        status.setRank(rank);
        return status;
    }

    @Override
    public UserLessonStatus loadLessonStatus(long userId, long courseId, long termId,
            long lessonId) {
        UserLessonStatus status = new UserLessonStatus();
        boolean isComplete = this.courseTermLessonIsComplete(userId, courseId, termId, lessonId);
        status.setIsComplete(isComplete);
        if (!isComplete) {
            return status;
        }
        long knowledge = this.loadCourseTermLessonKnowledge(userId, courseId, termId, lessonId);
        long exp = knowledge * BASE_KNOWLEDGE_EXP;
        status.setExp(exp);
        int questionCount = lessonData.getQuestionCount(lessonId);
        double correctPercent = knowledge * 1.0 / questionCount;
        
        status.setCorrectPercent(correctPercent);
        return status;
    }
    
    private int buildUserRankValue(long userId, List<RankInfo> rankInfoList) {
        for(RankInfo rankInfo:rankInfoList) {
            if(rankInfo.getUserId() == userId) {
                return rankInfo.getRank();
            }
        }
        return 0;
    }

    @Override
    public int loadRankValue(long userId) {
        long friendCount = cUserFriendService.getFriendCount(userId);
        List<Long> friendIdList = cUserFriendService.getFriendIds(userId, 0, friendCount - 1);
        friendIdList.add(userId);
        List<Long> friendExpList = this.loadExp(friendIdList);
        List<RankInfo> rankInfoList = buildRankList(friendIdList, friendExpList);
        return buildUserRankValue(userId, rankInfoList);
    }

    @Override
    public int loadCourseTermRankValue(long userId, long courseId, long termId) {
        // TODO
        //List<Long> courseTermUserIdList = new ArrayList<>();
        
        List<Long> courseTermUserIdList=userService.getUserByCourseIdTermId(courseId,termId);
        
        List<Long> expList = this.loadCourseTermExp(courseTermUserIdList, courseId, termId);
        List<RankInfo> rankInfoList = buildRankList(courseTermUserIdList, expList);
        return buildUserRankValue(userId, rankInfoList);
    }

    private List<UserRank> buildUserRankList(List<RankInfo> rankInfoList) {
        if (null == rankInfoList || rankInfoList.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserRank> result = new ArrayList<>();
        List<Long> userIdList = new ArrayList<>();
        for (RankInfo rankInfo : rankInfoList) {
            userIdList.add(rankInfo.getUserId());
        }
        List<UserInfo> userInfoList = userService.loadUserInfo(userIdList);
        for (int i = 0; i < rankInfoList.size(); ++i) {
            UserRank userRank = new UserRank();
            userRank.setUserInfo(userInfoList.get(i));
            userRank.setRankInfo(rankInfoList.get(i));
            result.add(userRank);
        }
        return result;
    }
    
    private List<RankInfo> buildRankList(List<Long> userIdList, List<Long> userExpList) {
        List<RankInfo> userRankList = new ArrayList<>();
        for (int i=0;i <userIdList.size(); ++i){
            RankInfo rankInfo = new RankInfo();
            rankInfo.setUserId(userIdList.get(i));
            rankInfo.setExp(userExpList.get(i));
            userRankList.add(rankInfo);
        }
        RankEntityCompartor compartor = new RankEntityCompartor();
        Collections.sort(userRankList, compartor);
        int realRank = 0;
        int rank = realRank;
        long lastExp = Long.MAX_VALUE;
        for (RankInfo rankInfo : userRankList) {
            ++realRank;
            if (rankInfo.getExp() != lastExp) {
                rank = realRank;
            }
            rankInfo.setRank(rank);
            lastExp = rankInfo.getExp();
        }
        return userRankList;
    }

    @Override
    public List<UserRank> loadRankList(long userId) {
        long friendCount = cUserFriendService.getFriendCount(userId);
        List<Long> friendIdList = cUserFriendService.getFriendIds(userId, 0, friendCount - 1);
        friendIdList.add(userId);
        List<Long> friendExpList = this.loadExp(friendIdList);
        List<RankInfo> rankInfoList = buildRankList(friendIdList, friendExpList);
        return buildUserRankList(rankInfoList);
    }

    @Override
    public List<UserRank> loadCourseTermRankList(long userId, long courseId, long termId) {
        // TODO
        //List<Long> courseTermUserIdList = userService.getUserIds(0, userService.getUserCount());//获取了所有用户的Id
        List<Long> courseTermUserIdList=this.getUserId(courseId,termId);//获取购买了该课程该期的用户们
        List<Long> expList = this.loadCourseTermExp(courseTermUserIdList, courseId, termId);//获取大家的exp
        List<RankInfo> rankInfoList = buildRankList(courseTermUserIdList, expList);		//构建排行	
        return buildUserRankList(rankInfoList);
    }

    @Override
    public List<UserRank> loadFCourseTermRankList(long userId, long courseId, long termId){
    	List<Long> courseTermUserIdList=this.getUserId(courseId,termId);
    	List<Long> fUserIdList=new ArrayList<>();
	    for(int i=0;i<courseTermUserIdList.size();i++){
	    	long tempId=courseTermUserIdList.get(i);//获取每个对象的userId     
	    	if(userCollegeService.checkFriend(userId,tempId)){ //判断是否好友
		    	fUserIdList.add(tempId);
	    	}
	    }
	    fUserIdList.add(userId);//用户自身加入list
	    List<Long> expList = this.loadCourseTermExp(fUserIdList, courseId, termId);//获取大家的exp
        List<RankInfo> rankInfoList = buildRankList(fUserIdList, expList);		//构建排行
	    
	    return buildUserRankList(rankInfoList);
    }

    @Override
    public List<UserRank> loadWeekRankList(long userId) {
        Date date = TimeUtils.getDate();
        long friendCount = cUserFriendService.getFriendCount(userId);
        List<Long> friendIdList = cUserFriendService.getFriendIds(userId, 0, friendCount - 1);
        friendIdList.add(userId);
        List<Long> friendExpList = this.loadWeekExp(friendIdList, date.getWeek());
        List<RankInfo> rankInfoList = buildRankList(friendIdList, friendExpList);
        return buildUserRankList(rankInfoList);
    }

    @Override
    public List<UserRank> loadCourseTermWeekRankList(long userId, long courseId, long termId) {
        Date date = TimeUtils.getDate();
        // TODO
        //List<Long> courseTermUserIdList = userService.getUserIds(0, userService.getUserCount());
        List<Long> courseTermUserIdList=this.getUserId(courseId,termId);
        List<Long> expList = this.loadCourseTermWeekExp(courseTermUserIdList, courseId, termId, date.getWeek());
        List<RankInfo> rankInfoList = buildRankList(courseTermUserIdList, expList);
        return buildUserRankList(rankInfoList);
    }
    /**
     * 购买课程
     * @author dusc
     */
     public void userPurchaseTerm(long userId,long courseId,long termId,double coins){
    	 
    	 userCourseTermMapper.userPurchaseTerm(userId,courseId, termId);
    	 userCourseTermMapper.reduceCoins(userId, coins);
    	 this.replaceCourseTermId(userId, courseId, termId, true);//redis课程用户关系 更新
    	 this.replaceUserCoins(userId,coins); //redis用户雁币更新
     }
     
     /**
      * 购买课程检验去重
      * @author dusc
      */
     
     public void userPurchase(long userId,long courseId,long termId,double coins){
    	 userCourseTermMapper.userPurchase(userId, courseId, termId, coins);
     }
     
 
	public List<BillsInfo> checkPurchase(long userId,long courseId,long termId){
    	 List<BillsInfo> billsinfo=userCourseTermMapper.checkPurchase(userId, courseId, termId);
    	 return billsinfo;
     }

	@Override
	public List<CourseInfo> getAllCourseInfo() {
		// TODO Auto-generated method stub
		return courseMapper.getAllCourseInfo();
	}
	
	 /**
     * 获取用户相关的课程id
     */
	@Override
	public List<UserTermCourseEntity> selectUserCourseTermByUserId(Long userId) {
		// TODO Auto-generated method stub
		return userCourseTermMapper.selectUserCourseTermByUserId(userId);
	}
	
	public int loadCourseTermRank(long userId,long courseId,long termId,List<UserRank> userRanks){
		for(int i=0;i<userRanks.size();i++){
			//从List中hash出userId对应的userInfo和rankInfo
			while (userId==userRanks.get(i).getRankInfo().getUserId())
				return userRanks.get(i).getRankInfo().getRank();
		}
		return userRanks.size();
	}
	
	
	public List<Long> getUserId(long courseId,long termId){
		return userCourseTermMapper.getUserId(courseId,termId);
	}
	
	public boolean checkFriend(long userId,long friendId){
    	List<UserFriendInfo> checkFriends =userCourseTermMapper.checkFriend(userId,friendId);
    	boolean fri=false;
    	if (checkFriends.size() !=0){   //索引到记录就是好友 所以返判友为真
    		if(checkFriends.get(0).getFriendId() != userId)
    			fri=true;
    	}
    	return fri;
    }
	
	public long getCourseIdByTermId(long termId){
		return userCourseTermMapper.getCourseIdByTermId(termId);
	}
}
