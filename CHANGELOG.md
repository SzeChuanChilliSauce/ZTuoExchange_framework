* # [Exchange]
 * 1、 取消盘口信息totalAmount(累计)字段，改由前端自由计算
    
* 2、 盘口信息推送每个方向最多返回10档

* ## Wallet     
* 1、 自动提币失败后转为人工处理

* ## Chat
* 1、 增加Netty实时聊天推送，IOS实现APNS推送  

* ## Market    
*  1、 支持Netty实时行情推送

 
* # [后台管理]
* ## 会员管理
* 1、会员管理添加状态筛选  

* ## 法币管理
* 1、广告管理添加状态筛选   
* 2、订单申诉分页有问题   显示有三页但是点击到其他页面都是空的
* 3,、认证商家筛选待审核状态下的表单，点击审核通过后应该保持当前筛选页面   
* 4、订单管理添加导出功能
* 5、内容管理添加按照序号排序功能，序号越大位置越靠前，前端展示顺序也一样

* ## 财务管理   
* 1、交易明细添加会员昵称、交易类型、交易金额。手续费、交易时间搜索
* 2、提现审核需要查询：昵称、提现币种、提现地址、提现方式、状态查询

* ## 钱包管理
* 1、新增转账地址维护功能


#  [JAVA]
* 增加cloud模块
* 添加chat模块netty支持，实现tcp实时消息通知
* 汇率获取模块对没有交易的币种支持人工定价
* 增加委托订单最低卖价限制、市价买卖开关
* 用户下单接口优化，增加下单前对会员状态校验
* 用户取消订单优化，增加强制取消订单功能
* 重新定义撮合交易器
* 修改交易链表改为TreeMap
* 批量发送交易记录，避免信息过大
* 修改trade深度
* 对推送盘口信息优化
* 处理交易明细优化
* 增加多线程处理交易明细
* 优化交易处理结果
* 线上bug修复
* 修复提示语
* 修复法币交易订单短信
* 修复用户币币地址管理
* 机器人下单优化

-------------------------------------------------------------------------------------------------
* # [Exchange]
 * 1. Cancel the totalAmount (accumulative) field of the handicap information and let the front end calculate freely
    
* 2. Handicap information push returns up to 10 gears in each direction

* ## Wallet
* 1. Turn to manual processing after automatic withdrawal fails

* ## Chat
* 1. Add Netty real-time chat push, IOS realizes APNS push

* ## Market
* 1. Support Netty real-time market push

 
* # [Backstage Management]
* ## Member Management
* 1. Member management add status filter

* ## Legal currency management
* 1. Add status filter to ad management
* 2. There is a problem with the order appeal page. Three pages are displayed but all other pages are empty when clicked.
* 3. The certified merchant screens the form under the pending review status, and the current screening page should be maintained after clicking the review passed
* 4. Add export function to order management
* 5. Content management adds the function of sorting by serial number, the larger the serial number, the higher the position, and the front-end display order is also the same

* ## Financial Management   
* 1. Add member nickname, transaction type and transaction amount to transaction details. Search fees and transaction time
* 2. Inquire about withdrawal review: nickname, withdrawal currency, withdrawal address, withdrawal method, status inquiry

* ## Wallet Management
* 1. Added transfer address maintenance function


# [JAVA]
* Add cloud module
* Add chat module netty support, realize tcp real-time message notification
* The exchange rate acquisition module supports manual pricing for currencies without transactions
* Add the minimum selling price limit for entrusted orders and the market price trading switch
* Optimized the user's order interface, and added verification of member status before placing an order
* Optimized user cancellation order, adding the function of forced order cancellation
* Redefine the matching transaction machine
* Modify the transaction list to TreeMap
* Send transaction records in batches to avoid excessive information
* Modify trade depth
* Optimize the push market information
* Process transaction details optimization
* Add multi-thread processing transaction details
* Optimize transaction processing results
* Online bug fix
* Fix prompt
* Fix the SMS of fiat currency transaction orders
* Fix user currency address management
* Robot order optimization