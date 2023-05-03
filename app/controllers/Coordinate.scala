package controllers

import play.api.mvc.Result

import lila.api.Context
import lila.app.{ given, * }

final class Coordinate(env: Env) extends LilaController(env):

  def home     = Open(serveHome)
  def homeLang = LangPage(routes.Coordinate.home)(serveHome(using _))
  private def serveHome(using ctx: Context): Fu[Result] =
    ctx.userId ?? { userId =>
      env.coordinate.api getScore userId map (_.some)
    } map { score =>
      views.html.coordinate.show(score)
    }

  def score =
    AuthBody { implicit ctx => me =>
      given play.api.mvc.Request[?] = ctx.body
      env.coordinate.forms.score
        .bindFromRequest()
        .fold(
          _ => fuccess(BadRequest),
          data => env.coordinate.api.addScore(me.id, data.mode, data.color, data.score) inject Ok(())
        )
    }
