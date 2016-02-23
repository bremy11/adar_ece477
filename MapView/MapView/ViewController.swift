//
//  ViewController.swift
//  MapView
//
//  Created by Tessie McInerney on 2/7/16.
//  Copyright © 2016 Tessie McInerney. All rights reserved.
//

import UIKit
import MapKit
import CoreLocation


class ViewController: UIViewController, MKMapViewDelegate, CLLocationManagerDelegate
{
    @IBOutlet weak var mapView: MKMapView!
    
    let locationManager = CLLocationManager()
    
    
    //BUTTON
    @IBAction func DeliverButton(sender: UIButton) {
        
        
        //Confirmation Alert
        let alert = UIAlertController(title: "", message: "Are you sure you want to order a delivery?", preferredStyle: UIAlertControllerStyle.Alert)
        
        alert.addAction(UIAlertAction(title: "Cancel", style: UIAlertActionStyle.Default, handler: nil))
        
        self.presentViewController(alert, animated: true, completion: nil)
        
        alert.addAction(UIAlertAction(title: "Yes", style: .Default, handler: { action in
            switch action.style{
            case .Default:
                
                //ADD CODE TO SEND TO SERVER
                self.initNetworkCommunication()
                
                self.dropAPin()
                
                self.successfulOrder()
                
            case .Cancel:
                print("cancel")
                
            case .Destructive:
                print("destructive")
            }
        }))
    }
    
    /*
    func initNetworkCommunication(){
        let addr = "127.0.0.1"
        let port = 9091
        
        //       var host :NSHost = NSHost(address: addr)
        var inp :NSInputStream?
        var out :NSOutputStream?
        
        NSStream.getStreamsToHostWithName(addr, port: port, inputStream: &inp, outputStream: &out)
        
        let inputStream = inp!
        let outputStream = out!
        
        inputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
        outputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
        
        inputStream.open()
        outputStream.open()
        
        //var readByte :UInt8 = 0
        //while inputStream.hasBytesAvailable {
        //    inputStream.read(&readByte, maxLength: 1)
        //}
        
        let st = "foo bar\n"
        //let data = string.dataUsingEncoding(NSUTF8StringEncoding, allowLossyConversion: false)!
        //let bytesWritten = outputStream.write(UnsafePointer(data.bytes), maxLength: data.length)
        
        //var writeByte :UInt8 = 11
        //var buffer = [UInt8](count: 8, repeatedValue: 1)
        
        // buffer is a UInt8 array containing bytes of the string "Jonathan Yaniv.".
        
        outputStream.write(st, maxLength: 8)
        
        var readByte :UInt8 = 0
        var x = true
        var i = 0
        var outStr = ""
        while x {
        while inputStream.hasBytesAvailable {
            //print(i)
            inputStream.read(&readByte, maxLength: 1)
            
            let u = UnicodeScalar(readByte)
            let char = String(u)
            //print(char)
            outStr += char
            i++
            x = false
        }
        }
        print(outStr)
        
    }*/
    
    func initNetworkCommunication(){
        let addr = "moore11.cs.purdue.edu"
        let port = 3112
        
        //       var host :NSHost = NSHost(address: addr)
        var inp :NSInputStream?
        var out :NSOutputStream?
        
        NSStream.getStreamsToHostWithName(addr, port: port, inputStream: &inp, outputStream: &out)
        
        let inputStream = inp!
        let outputStream = out!
        
        inputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
        outputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
        
        inputStream.open()
        outputStream.open()
        
        //var readByte :UInt8 = 0
        //while inputStream.hasBytesAvailable {
        //    inputStream.read(&readByte, maxLength: 1)
        //}
        
        let dataStr = "13\n"
        //let data = string.dataUsingEncoding(NSUTF8StringEncoding, allowLossyConversion: false)!
        //let bytesWritten = outputStream.write(UnsafePointer(data.bytes), maxLength: data.length)
        
        //var writeByte :UInt8 = 10
        //var buffer = [UInt8](count: 8, repeatedValue: 1)
        
        // buffer is a UInt8 array containing bytes of the string "Jonathan Yaniv.".

        outputStream.write(dataStr , maxLength: 3)
        
        //json build test
        let validDictionary = [
            "lat": "46",
            "longe": "43"
        ]
        
        
        
        if NSJSONSerialization.isValidJSONObject(validDictionary) { // True
            do {
                //let rawData = try NSJSONSerialization.dataWithJSONObject(validDictionary, options: .PrettyPrinted)
                //print(rawData)
                //outputStream.write(UnsafePointer<UInt8>(rawData.bytes) , maxLength: 1024)
                let send = "\(validDictionary)\n"
                print(send)
                outputStream.write(send, maxLength:1024);
                let newLine = "\n"
                outputStream.write(newLine , maxLength: 1)
                //print("\n")
            } catch {
                // Handle Error
            }
            
        }else{
            print("Invalid JSON data")
            
        }
        
        /*
let jsonObject: [String: AnyObject] =
[
"lat": "46",
"longe": "43"
]

if NSJSONSerialization.isValidJSONObject(jsonObject) { // True
do {
//let rawData = try NSJSONSerialization.dataWithJSONObject(jsonObject, options: .PrettyPrinted)
// print(rawData)

//let send = "\(jsonObject)\n"
print(send)
outputStream.write(send , maxLength: 1024)
} catch {
// Handle Error
}

}else{
print("Invalid JSON data")

}
*/

        var readByte :UInt8 = 0
        var x = true
        var i = 0
        var outStr = ""
        while x {
            while inputStream.hasBytesAvailable {
                //print(i)
                inputStream.read(&readByte, maxLength: 1)
                
                let u = UnicodeScalar(readByte)
                
                let char = String(u)
                print(char)
                
                
                outStr += char
                i++
                if (char == "\n"){
                    x = false
                    break
                }
                //x = false
            }
        }
        print(outStr)
       
        
        do {
            let json = try NSJSONSerialization.JSONObjectWithData(outStr.dataUsingEncoding(NSUTF8StringEncoding)!, options: .AllowFragments)
            print(json)
            /*
            if let blogs = json["blogs"] as? [[String: AnyObject]] {
                for blog in blogs {
                    if let name = blog["name"] as? String {
                        names.append(name)
                    }
                }
            }*/
            
        } catch {
            print("error serializing JSON: \(error)")
        }
        
        //print(names) // ["Bloxus test", "Manila Test"]
    }
    
    
    
    
    func successfulOrder() {
        //Second alert message -- Successful Order
        let alert = UIAlertController(title: "", message: "Your order is on it's way! Head to the dropped pin!", preferredStyle: UIAlertControllerStyle.Alert)
        
        alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default, handler: nil))
        
        self.presentViewController(alert, animated: true, completion: nil)
    }
    
    func dropAPin() {
        //Waypoint 1
        let waypoint1 = CLLocationCoordinate2DMake(40.42935, -86.9145)
        let pinDrop1 = MKPointAnnotation()
        pinDrop1.coordinate = waypoint1
        pinDrop1.title = "Waypoint 1"  //this box needs to be edited -- ID: ______ <- edit this
        mapView.addAnnotation(pinDrop1)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.locationManager.delegate = self
        
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest
        
        self.locationManager.requestWhenInUseAuthorization()
        
        self.locationManager.startUpdatingLocation()
        
        self.mapView.showsUserLocation = true
        
        /*Waypoint 1
        let waypoint1 = CLLocationCoordinate2DMake(40.42935, -86.9145)
        let pinDrop1 = MKPointAnnotation()
        pinDrop1.coordinate = waypoint1
        pinDrop1.title = "Waypoint 1"
        mapView.addAnnotation(pinDrop1)
        
        
        //Waypoint 2
        let waypoint2 = CLLocationCoordinate2DMake(40.42983, -86.915)
        let pinDrop2 = MKPointAnnotation()
        pinDrop2.coordinate = waypoint2
        pinDrop2.title = "Waypoint 2"
        mapView.addAnnotation(pinDrop2)
        
        //Waypoint 3
        let waypoint3 = CLLocationCoordinate2DMake(40.43075, -86.916)
        let pinDrop3 = MKPointAnnotation()
        pinDrop3.coordinate = waypoint3
        pinDrop3.title = "Waypoint 3"
        mapView.addAnnotation(pinDrop3)
        */
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //MARK: - Location Delegate Methods
    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        let location = locations.last
        
        let center = CLLocationCoordinate2D(latitude: location!.coordinate.latitude, longitude: location!.coordinate.longitude)
        
        let region = MKCoordinateRegion(center: center, span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))
        
        self.mapView.setRegion(region, animated: true)
        
        self.locationManager.stopUpdatingLocation()
        
        let locValue:CLLocationCoordinate2D = manager.location!.coordinate
        print("locations =  latitude: \(locValue.latitude) longitude: \(locValue.longitude)")
    }
    
    
    func locationManager(manager: CLLocationManager, didFailWithError error: NSError)
    {
        print("Errors " + error.localizedDescription)
    }
    
}

