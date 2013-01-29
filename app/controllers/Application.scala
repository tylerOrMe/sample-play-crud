package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import anorm._

import views._
import models._

/**
 * Manage a database of cars
 */
object Application extends Controller { 
  
  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.list(0, 2, ""))
  
  /**
   * Describe the car form (used in both edit and create screens).
   */ 
  val carForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "model" -> nonEmptyText,
      "brand" -> optional(longNumber),
      "price" -> optional(of[Double])
    )(Car.apply)(Car.unapply)
  )
  
  // -- Actions

  /**
   * Handle default path requests, redirect to cars list
   */  
  def index = Action { Home }
  
  /**
   * Display the paginated list of cars.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on car names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.list(
      Car.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }
  
  /**
   * Display the 'edit form' of a existing Car.
   *
   * @param id Id of the car to edit
   */
  def edit(id: Long) = Action {
    Car.findById(id).map { car =>
      Ok(html.editForm(id, carForm.fill(car), Brand.options))
    }.getOrElse(NotFound)
  }
  
  /**
   * Handle the 'edit form' submission 
   *
   * @param id Id of the car to edit
   */
  def update(id: Long) = Action { implicit request =>
    carForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.editForm(id, formWithErrors, Brand.options)),
      car => {
        Car.update(id, car)
        Home.flashing("success" -> "Car %s has been updated".format(car.model))
      }
    )
  }
  
  /**
   * Display the 'new car form'.
   */
  def create = Action {
    Ok(html.createForm(carForm, Brand.options))
  }
  
  /**
   * Handle the 'new car form' submission.
   */
  def save = Action { implicit request =>
    carForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.createForm(formWithErrors, Brand.options)),
      car => {
        Car.insert(car)
        Home.flashing("success" -> "Car %s has been created".format(car.model))
      }
    )
  }
  
  /**
   * Handle car deletion.
   */
  def delete(id: Long) = Action {
    Car.delete(id)
    Home.flashing("success" -> "Car has been deleted")
  }

}
            
