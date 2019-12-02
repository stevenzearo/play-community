package controllers.demo

import controllers.component.{AuthConstants, ComponentsController}
import infrastructure.repository.mongo.MongoUserRepository
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * permission 基于资源角色的权限控制测试接口
  *
  * 登录前：http://localhost:9000/demo/permission    无权限
  * 登录后：http://localhost:9000/demo/permission   正常操作，此处反回User信息
  * {{{
  *   {
  * "_id": "1",
  * "role": "user",
  * "login": "568845948@qq.com",
  * "password": "950a5c4c4dc5c91c117548511f6ba6edadbe55d5d883b520523f974026503580",
  * "setting": {
  * "name": "MJML",
  * "pinyin": "MJML",
  * "gender": "",
  * "introduction": "",
  * "headImg": "/assets/images/head.png",
  * "city": ""
  * },
  * "stat": {
  * "resCount": 0,
  * "docCount": 0,
  * "articleCount": 0,
  * "qaCount": 0,
  * "fileCount": 0,
  * "replyCount": 0,
  * "commentCount": 0,
  * "voteCount": 0,
  * "votedCount": 0,
  * "createTime": "2019-12-02T14:59:39.786Z",
  * "updateTime": "2019-12-02T14:59:39.786Z",
  * "lastLoginTime": "2019-12-02T14:59:39.786Z",
  * "lastReplyTime": "2019-12-02T14:59:39.786Z"
  * },
  * "score": 0,
  * "enabled": true,
  * "from": "register",
  * "ip": "0:0:0:0:0:0:0:1",
  * "channels": [],
  * "salt": "3KCe2kxhc8SYSRiMtd0RZw==",
  * "argon2Hash": "$argon2i$v=19$m=1024,t=2,p=2$M0tDZTJreGhjOFNZU1JpTXRkMFJadz09$YvOyDtRoXMNu4Jlc/o1wkeEqpK/6ybqfLwXi9UqBvQo"
  * }
  * }}}
  */
@Singleton
class PermissionController @Inject()(userRepo: MongoUserRepository)(
    implicit cc: ControllerComponents,
    ce: ExecutionContext)
    extends ComponentsController(userRepo)(ce, cc) {

  def index = LoginUserAuthAction(AuthConstants.ROOT).async { implicit request =>
    Future.successful(Ok(Json.toJson(request.user)))
  }
}
