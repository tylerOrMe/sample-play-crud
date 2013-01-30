package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Brand(id: Pk[Long] = NotAssigned, name: String)
case class Car(id: Pk[Long] = NotAssigned, model: String, brandId: Option[Long], price:Option[Double])

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Car {
  
  // -- Parsers
  
  /**
   * Parse a Car from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("car.id") ~
    get[String]("car.model") ~
    get[Option[Long]]("car.brand_id") ~
    get[Option[Double]]("car.price") map {
      case id~model~brand~price => Car(id, model, brand, price)
    }
  }
  
  /**
   * Parse a (Car,Brand) from a ResultSet
   */
  val withBrand = Car.simple ~ (Brand.simple ?) map {
    case car~brand => (car,brand)
  }
  
  // -- Queries
  
  /**
   * Retrieve a cars from the id.
   */
  def findById(id: Long): Option[Car] = {
    DB.withConnection { implicit connection =>
      SQL("select * from car where id = {id}").on('id -> id).as(Car.simple.singleOpt)
    }
  }
  
  /**
   * Return a page of (Car,Brand).
   *
   * @param page Page to display
   * @param pageSize Number of cars per page
   * @param orderBy Car property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Car, Option[Brand])] = {
    
    val offest = pageSize * page
    
    DB.withConnection { implicit connection =>
      
      val cars = SQL(
        """
          select * from car 
          left join brand on car.brand_id = brand.id
          where car.model like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize, 
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Car.withBrand *)

      val totalRows = SQL(
        """
          select count(*) from car 
          left join brand on car.brand_id = brand.id
          where car.model like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(cars, page, offest, totalRows)
      
    }
    
  }
  
  /**
   * Update a cars.
   *
   * @param id The cars id
   * @param cars The cars values.
   */
  def update(id: Long, cars: Car) = {
    println(s"$cars, id $id")
    DB.withConnection { implicit connection =>
      SQL(
        """
          update car
          set model = {model}, brand_id = {brand_id}, price = {price}
          where id = {id}
        """
      ).on(
        'model -> cars.model,
        'brand_id -> cars.brandId,
        'price -> cars.price,
        'id -> id
      ).executeUpdate()
    }
  }
  
  /**
   * Insert a new cars.
   *
   * @param cars The cars values.
   */
  def insert(cars: Car) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into car values (
            (select next value for car_seq), 
            {model},  {price}, {brand_id}
          )
        """
      ).on(
        'model -> cars.model,
        'brand_id -> cars.brandId,
        'price -> cars.price
      ).executeUpdate()
    }
  }
  
  /**
   * Delete a cars.
   *
   * @param id Id of the cars to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from car where id = {id}").on('id -> id).executeUpdate()
    }
  }
  
}

object Brand {
    
  /**
   * Parse a Brand from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("brand.id") ~
    get[String]("brand.name") map {
      case id~name => Brand(id, name)
    }
  }
  
  /**
   * Construct the Map[String,String] needed to fill a select options set.
   */
  def options: Seq[(String,String)] = DB.withConnection { implicit connection =>
    SQL("select * from brand order by name").as(Brand.simple *).map(c => c.id.toString -> c.name)
  }
  
}

