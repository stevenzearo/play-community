package controllers.component

import infrastructure.repository.mongo.MongoUserRepository
import models.User
import play.api.libs.json.Json
import play.api.mvc._
import utils.RequestHelper

import scala.concurrent.{ExecutionContext, Future}

/**
  * 统一封装Play的json解析，表单，鉴权
  * @author 梦境迷离
  * @since 2019-12-02
  * @version v1.0
  */
class ComponentsController(userRepo: MongoUserRepository)(
    implicit ce: ExecutionContext,
    cc: ControllerComponents)
    extends BaseController {
  import AuthConstants._

  override protected def controllerComponents: ControllerComponents = cc

  class UserRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)
  class AuthRequest[A](val role: String, userRequest: UserRequest[A]) extends UserRequest(userRequest.user, userRequest)

  object AuthAction extends ActionBuilder[UserRequest, AnyContent] with ActionRefiner[Request, UserRequest] {
    override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser //意味着根据Content-Type header自动解析
    override protected def refine[A](
        request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
      userRepo.findById(request.session("uid")).map {
        case Some(u) => Right(new UserRequest(u, request))
        case None => Left(Results.NotFound)
      }

    }

    override protected def executionContext: ExecutionContext = ce
  }

  def AuthRequestTransformer: ActionTransformer[UserRequest, AuthRequest] =
    new ActionTransformer[UserRequest, AuthRequest] {
      override protected def transform[A](request: UserRequest[A]): Future[AuthRequest[A]] = {
        //查询用户角色，并生成带角色信息的request
        Future.successful(new AuthRequest[A]("root", request))
      }

      override protected def executionContext: ExecutionContext = ce
    }

  def CheckLogin[A] = new ActionBuilder[Request, AnyContent] with Rendering with AcceptExtractors {
    override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
    override protected def executionContext: ExecutionContext = ce
    override def invokeBlock[A](
        request: Request[A],
        block: (Request[A]) => Future[Result]): Future[Result] = {
      if (RequestHelper.isLogin(request)) {
        block(request)
      } else {
        Future.successful {
          render {
            case Accepts.Html() => Results.Ok(views.html.message("系统提示", "您尚未登录，无权执行该操作！")(request))
            case Accepts.Json() => Results.Ok(Json.obj("status" -> 1, "msg" -> "您尚未登录，无权执行该操作！"))
          }(request)
        }
      }
    }
  }

  def PermissionCheckAction(permission: String): ActionFunction[UserRequest, AuthRequest] =
    AuthRequestTransformer andThen new ActionFilter[AuthRequest] {
      override protected def filter[A](request: AuthRequest[A]): Future[Option[Result]] = {
        //根据前面action拿到的角色和传进来的permission进行权限验证，若无permission级的控制则去掉
        if (permission == "root") {
          //root或无权限的，直接通过
          Future.successful(None)
        } else {
          //其他角色需要查询db
          Future.successful(None)
        }
      }

      override protected def executionContext: ExecutionContext = ce
    }

  //1.session是否有用户信息
  //2.数据库是否能查询到用户信息
  //3.用户是否具有响应的权限或角色
  def LoginUserAuthAction(permission: String = ALL_ALLOWED) =
    CheckLogin andThen AuthAction andThen PermissionCheckAction(permission)
}

object AuthConstants {
  val ROOT = "root"
  val ALL_ALLOWED = "all_allowed"
}
