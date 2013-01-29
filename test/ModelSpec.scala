package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ModelSpec extends Specification {
  
  import models._

  "Car model" should {
    
    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val Some(sandero) = Car.findById(2)
      
        sandero.model must equalTo("Sandero")
        sandero.price must beEqualTo(12350)  
        
      }
    }
    
    
    "be updated if needed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        Car.update(2, Car(model="The Sandero", brandId=Some(1), price=None))
        
        val Some(sandero) = Car.findById(2)
        
        sandero.model must equalTo("The Sandero")
        sandero.price must beNone
        
      }
    }
    
  }
  
}