-- 更新板块名称和描述为更真实的内容
UPDATE section SET name='校园综合', description='校园生活大小事，想说就说！' WHERE id=1;
UPDATE section SET name='学习交流', description='学习经验、资料分享、考研考证，一起进步！' WHERE id=2;
UPDATE section SET name='二手交易', description='闲置物品转让，好东西别浪费！' WHERE id=3;
UPDATE section SET name='生活服务', description='租房、拼车、失物招领，互帮互助！' WHERE id=4;
UPDATE section SET name='活动广场', description='社团招募、活动报名、赛事通知都在这！' WHERE id=5;
UPDATE section SET name='美食分享', description='食堂测评、外卖推荐、烹饪秘籍，吃货集合！' WHERE id=6;

-- 插入测试用户（密码都是demo123）
INSERT IGNORE INTO user (id, username, password, nickname, avatar, role, status, created_at) VALUES
(2, 'demo', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE.B3P3vTlAAdW.dq', '小明同学', NULL, 0, 1, NOW()),
(3, 'lihua', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE.B3P3vTlAAdW.dq', '李华', NULL, 0, 1, NOW()),
(4, 'xiaomei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE.B3P3vTlAAdW.dq', '小美学姐', NULL, 0, 1, NOW()),
(5, 'zhangshuai', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE.B3P3vTlAAdW.dq', '张帅', NULL, 0, 1, NOW());

-- 插入帖子（综合板块）
INSERT IGNORE INTO post (id, user_id, section_id, title, content, status, view_count, like_count, comment_count, created_at) VALUES
(1, 2, 1, '新生入学必看！这些校园潜规则你知道吗？', '作为一个过来人，给大家整理了一些入学后才会知道的"潜规则"：\n\n1. 图书馆3楼靠窗位置是学霸聚集地，建议期末前两周提前去占位\n2. 食堂A区的红烧肉只有周二周四才有，错过再等一周\n3. 体育课选课要手速快，瑜伽和台球永远秒没\n4. 宿舍楼门禁晚上11点，不是10点（新生经常搞错）\n5. 学生证挂失补办要去行政楼203，不是保卫处\n\n还有什么补充的欢迎评论！', 1, 328, 56, 12, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 1, '今天在操场捡到一串钥匙，有失主吗？', '今天下午大约4点在操场北门附近捡到一串钥匙，上面有个小熊猫挂件，应该是宿舍钥匙+一把小锁的组合。\n\n失主请联系我，私信或者评论都可以，验证一下就还给你！别着急哈😊', 1, 89, 3, 8, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(3, 4, 1, '吐槽一下学校wifi，真的受不了了', '每次到晚上9点之后宿舍wifi就开始"表演"，加载个图片都要转圈圈，追个剧更是别想了。\n\n有没有同学知道哪个宿舍楼信号比较好？或者有什么提速的办法？\n\n已经考虑买流量包了，但感觉不应该啊，交了网费的😤', 1, 412, 89, 34, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 插入帖子（学习交流）
INSERT IGNORE INTO post (id, user_id, section_id, title, content, status, view_count, like_count, comment_count, created_at) VALUES
(4, 5, 2, '考研经验贴｜二战上岸985，分享我的备考心路', '经历了两年备考，终于在今年成功上岸！把我的经验分享给大家：\n\n【时间规划】\n- 3月-6月：打基础，过一遍专业课教材\n- 7月-9月：强化阶段，刷真题+做笔记\n- 10月-12月：冲刺阶段，模拟考试+查漏补缺\n\n【英语】\n单词是关键，我用的是朱伟的恋练有词，每天背50个，不能断！\n作文要背模板，但要改成自己的语言，别直接抄。\n\n【数学】\n张宇的基础30讲+660题，真的是神书！\n\n有问题的同学可以在评论区问我，能回答的都回答👍', 1, 892, 234, 67, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 2, 2, '求推荐高数参考书，大一小白求助', '本学期开始学高数，老师讲得很快跟不上，想找一本课外参考书自学。\n\n网上说同济版、高鸿业版都挺好的，但我不知道适不适合自学。\n\n大佬们有什么推荐吗？最好是讲解比较详细、例题多的那种💪', 1, 156, 28, 19, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(6, 3, 2, '四六级备考群，一起打卡！', '六月四六级马上到了，建了个备考群，每天打卡背单词、分享真题解析。\n\n群规：每天打卡，连续三天不打卡自动退群。严格管理，拒绝摸鱼！\n\n有意向的同学评论留言，我私信你群号，一起冲！💪💪', 1, 267, 45, 31, DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- 插入帖子（二手交易）
INSERT IGNORE INTO post (id, user_id, section_id, title, content, status, view_count, like_count, comment_count, created_at) VALUES
(7, 4, 3, '出闲置教材一批，线性代数/概率论/大学物理', '毕业清仓，以下教材九成新，几乎没有笔记：\n\n📚 线性代数（同济第7版）—— 15元\n📚 概率论与数理统计（浙大第5版）—— 15元\n📚 大学物理（上下册，马文蔚版）—— 20元一套\n📚 C语言程序设计（谭浩强版）—— 10元\n\n支持面交，地点可约图书馆门口或学生活动中心。\n私信或评论联系我，先到先得！', 1, 203, 12, 15, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 5, 3, '转让九成新自行车，捷安特入门款，380出', '买来骑了半年，现在不太需要了转给有需要的同学。\n\n品牌：捷安特ATX 830\n颜色：哑光黑\n状态：车架无划痕，轮胎磨损正常，变速正常\n原价：680，380出，可小刀但别太过分😅\n\n有意向私信，可约看车，学校东门停车场那边。', 1, 178, 8, 6, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 插入帖子（活动广场）
INSERT IGNORE INTO post (id, user_id, section_id, title, content, status, view_count, like_count, comment_count, created_at) VALUES
(9, 3, 5, '【招募】吉他社纳新！零基础也可以来', '🎸 青山吉他社2024年秋季纳新正式开始！\n\n无论你是零基础小白还是有一定基础的同学，我们都欢迎！社团有专业学长学姐手把手教学。\n\n【社团活动】\n- 每周三晚7点社团练习\n- 每学期举办一次小型演出\n- 不定期举办露天弹唱活动\n\n【报名方式】\n扫描下方二维码或者直接来学生活动中心306找我们！\n\n名额有限，先到先得🎵', 1, 445, 78, 23, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(10, 2, 5, '篮球友谊赛报名｜3V3，奖品丰厚！', '🏀 本周六下午2点，计算机学院VS经管学院篮球友谊赛！\n\n规则：3V3半场，每队报名5人（含1替补），循环赛制。\n\n奖品：\n- 冠军：运动耳机一人一个\n- 亚军：运动水壶一人一个\n- 参与奖：定制T恤\n\n报名截止本周四晚12点，在这个帖子下面评论【报名+姓名+学号】即可，满10队开赛！', 1, 312, 67, 42, DATE_SUB(NOW(), INTERVAL 8 HOUR));

-- 插入帖子（美食分享）
INSERT IGNORE INTO post (id, user_id, section_id, title, content, status, view_count, like_count, comment_count, created_at) VALUES
(11, 4, 6, '食堂暗黑料理大赏，你吃过几个？', '在食堂吃了两年，总结出以下"暗黑料理"排行榜，引发了强烈的情感共鸣🤣\n\nTop1：西红柿炒鸡蛋——每次下手都不一样，有时甜有时咸，充满惊喜\nTop2：红烧茄子——茄子吸油量惊人，一口下去整个人都在发光\nTop3：蒜蓉炒青菜——蒜蓉比青菜多，大蒜爱好者狂喜\nTop4：土豆炖排骨——土豆是主角，排骨是装饰\n\n但说实话，B区窗口的担担面真的很好吃，强烈推荐！大家有什么必吃必避的也分享一下！', 1, 567, 123, 48, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(12, 5, 6, '自制寝室宵夜教程｜电热杯能做的神仙宵夜', '宿舍不能用明火？没关系！一个电热杯就够了！\n\n今天分享几个我实测可行的电热杯食谱：\n\n🍜 泡面升级版：煮泡面时加一个鸡蛋+火腿肠+生菜，比外面的贵了好几倍的感觉\n🥣 燕麦粥：燕麦+牛奶+蜂蜜，早餐宵夜都完美\n🍵 红糖姜茶：生姜片+红糖+热水，冬天暖胃神器\n🌽 玉米：冷冻玉米棒直接加水煮，甜到爆\n\n注意：一定要用学校允许的小功率电热杯，安全第一！', 1, 389, 92, 27, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 更新板块帖子数量
UPDATE section SET post_count = (SELECT COUNT(*) FROM post WHERE section_id = section.id AND status = 1);

-- 插入几条评论
INSERT IGNORE INTO comment (id, target_id, target_type, user_id, content, status, like_count, created_at) VALUES
(1, 1, 'post', 3, '太有用了！刚入学的小萌新表示已收藏📌', 1, 12, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 1, 'post', 4, '补充一个：选修课要选口碑好的老师，学校官方App里有评分，刷一下再选', 1, 34, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 4, 'post', 2, '学长太强了！请问数学用张宇还是李永乐好？', 1, 5, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 4, 'post', 5, '二战成功太励志了！请问专业课怎么备考的？', 1, 8, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 9, 'post', 2, '报名！我要加入！有没有要求一定会弹的', 1, 3, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(6, 10, 'post', 3, '报名 + 张三 + 20241001，冲冲冲！🏀', 1, 6, DATE_SUB(NOW(), INTERVAL 7 HOUR)),
(7, 11, 'post', 2, '哈哈哈B区担担面确实好吃！但是C区的麻辣烫也不错呀', 1, 15, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 7, 'post', 3, '概率论还有吗？我需要！', 1, 2, DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- 插入公告
INSERT IGNORE INTO announcement (id, title, content, admin_id, is_active, created_at) VALUES
(1, '欢迎来到校园论坛！', '亲爱的同学们，校园活动发布与交流平台正式上线啦！🎉\n\n在这里你可以：\n· 发现和报名参与各类校园活动\n· 在各板块发帖交流，分享生活\n· 使用AI助手获取活动信息\n\n希望大家积极参与，共建温暖的校园社区！如有问题请联系管理员。', 1, 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, '关于文明上网的温馨提示', '为维护良好的社区环境，请大家遵守以下规定：\n1. 尊重他人，不得发布侮辱、诽谤等内容\n2. 二手交易请注意防骗，建议面交\n3. 涉及个人隐私的信息请谨慎发布\n\n违规内容将被删除，情节严重者将封号处理。感谢配合！', 1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY));
