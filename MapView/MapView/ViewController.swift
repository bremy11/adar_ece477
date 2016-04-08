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

var currentLat : String = String()
var currentLong : String = String()

class ViewController: UIViewController, MKMapViewDelegate, CLLocationManagerDelegate
{
    @IBOutlet weak var mapView: MKMapView!
    
    let locationManager = CLLocationManager()

    

    
    @IBAction func EditButton(sender: UIButton) {
        
        var idText : String = String()
        var adjText : String = String()
        
        let editAlert = UIAlertController(title: "", message: "Enter the waypoint id number you'd like to edit", preferredStyle: UIAlertControllerStyle.Alert)
        
        editAlert.addTextFieldWithConfigurationHandler({ (textField) -> Void in
            textField.text = "Example: 1"
        })
        
        editAlert.addAction(UIAlertAction(title: "OK", style: .Default, handler: { (action) -> Void in
            
            let textField = editAlert.textFields![0] as UITextField
            print("Text field: \(textField.text)")
            idText = textField.text!
            
            //adjacency edit alert
            let adjAlert = UIAlertController(title: "", message: "Enter the adjacent waypoint numbers in a comma separated list.", preferredStyle: UIAlertControllerStyle.Alert)
            
            adjAlert.addTextFieldWithConfigurationHandler({ (textField) -> Void in
                textField.text = "Example: 2,3"
            })
            
            adjAlert.addAction(UIAlertAction(title: "OK", style: .Default, handler: { (action) -> Void in
                let adjtextField = adjAlert.textFields![0] as UITextField
                print("Text field: \(adjtextField.text)")
                adjText = adjtextField.text!
                print("idText: \(idText) , adjText: \(adjText)")
                self.editAdjListNetworkConnection(idText, adjList: adjText)
            }))
            
            self.presentViewController(adjAlert, animated: true, completion: nil)
            //end of adj edit alert
            
         
        }))
        
        self.presentViewController(editAlert, animated: true, completion: nil)
        
        //print("idText: \(idText) , adjText: \(adjText)")
        
        
    }
    
    //BUTTONS
    @IBAction func AddButton(sender: UIButton) {
        
        
        let alert2 = UIAlertController(title: "", message: "Adding a waypoint at your current location. Enter the adjacent waypoints in a comma separated list", preferredStyle: UIAlertControllerStyle.Alert)
        
        alert2.addTextFieldWithConfigurationHandler({ (textField) -> Void in
            textField.text = "Example: 1,3"
        })
        
        alert2.addAction(UIAlertAction(title: "OK", style: .Default, handler: { (action) -> Void in
            let textField = alert2.textFields![0] as UITextField
            print("Text field: \(textField.text)")
            //self.addWaypointNetworkConnection(textField.text!)
            self.getWaypoints()
        }))
        
        self.presentViewController(alert2, animated: true, completion: nil)
        
        
        /*print("Current Location: \(currentLat) , \(currentLong)")
        //self.addWaypointNetworkConnection()
        
         alert2.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default, handler: nil))
        
            self.presentViewController(alert2, animated: true, completion: nil)
        */
        
    }

    @IBAction func DeliverButton(sender: UIButton) {
        
        
        //Confirmation Alert
        let alert = UIAlertController(title: "", message: "Are you sure you want to order a delivery?", preferredStyle: UIAlertControllerStyle.Alert)
        
        alert.addAction(UIAlertAction(title: "Cancel", style: UIAlertActionStyle.Default, handler: nil))
        
        self.presentViewController(alert, animated: true, completion: nil)
        
        alert.addAction(UIAlertAction(title: "Yes", style: .Default, handler: { action in
            switch action.style{
            case .Default:
                
                print("Deliver Button Pressed")
                
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
    
    func displayWaypoints( lat: Double, long: Double, num: Int) {
        
        let waypoint = CLLocationCoordinate2DMake(lat, long)
        let pinDrop = MKPointAnnotation()
        pinDrop.coordinate = waypoint
        pinDrop.title = "\(num)"
        mapView.addAnnotation(pinDrop)
    }
    
    
    
    func getWaypoints(){
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
        
        print("10\n")
        let dataStr = "10\n" //change to get waypoints number
        
        outputStream.write(dataStr , maxLength: 3)
        
        //json build test
        /*let validDictionary = [
            "lat": "\(currentLat)",
            "longe": "\(currentLong)"
        ]
        //print("valid Dictionary \(validDictionary)")
        
        
        if NSJSONSerialization.isValidJSONObject(validDictionary) { // True
            do {
                //let rawData = try NSJSONSerialization.dataWithJSONObject(validDictionary, options: .PrettyPrinted)
                //print(rawData)
                //outputStream.write(UnsafePointer<UInt8>(rawData.bytes) , maxLength: 1024)
                print("Waypoint Addition at - \(validDictionary)")
                let send = "\(validDictionary)\n"
                outputStream.write(send, maxLength:1024);
                let newLine = "\n"
                outputStream.write(newLine , maxLength: 1)
                //print("\n")
            } catch {
                // Handle Error
            }
            
        }else{
            print("Invalid JSON data")
            
        }*/
        
        //inputStream.close()
        //outputStream.close()
        var i = 1;
        var numPoints = 10;
        
        while ( i <= 15)
        {
            print("here \(i)")
        var readByte :UInt8 = 0
        var x = true
        var outStr = ""
        while x {
            while inputStream.hasBytesAvailable {
                //print(i)
                inputStream.read(&readByte, maxLength: 1)
                
                let u = UnicodeScalar(readByte)
                
                let char = String(u)
                //print(char)
                
                
                outStr += char
                //i++
                if (char == "\n"){
                    x = false
                    break
                }
                //x = false
            }
        }
        print(outStr)
        
        do {
            
            let data: NSData = outStr.dataUsingEncoding(NSUTF8StringEncoding, allowLossyConversion: false)!
            //let json = try NSJSONSerialization.JSONObjectWithData(outStr, options: [])
            let boardsDictionary: NSDictionary = try NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers) as! NSDictionary
            
            //let json = try NSJSONSerialization.JSONObjectWithData(data, options: .AllowFragments)
            print(boardsDictionary)
            print(boardsDictionary["numPoints"]!)
            let pinLatString = String(boardsDictionary["lat"]!)
            let pinLongString = String(boardsDictionary["longe"]!)
            let pinNumString = String(boardsDictionary["id"]!)
            
            let pinLat = Double(pinLatString)
            let pinLong = Double(pinLongString)
            let pinNum = Int(pinNumString)
            
            //print("pinLat: \(pinLat) , pinLong: \(pinLong) , pinNum: \(pinNum)")
            self.displayWaypoints(pinLat!, long: pinLong!, num: pinNum!)
            
            
            /*
            if let blogs = json["blogs"] as? [[String: AnyObject]] {
            for blog in blogs {
            if let name = blog["name"] as? String {
            names.append(name)
            }
            }
            }*/
           

            
            
            //if let points = json["numPoints"] as? [String]{
            //    print("in 3")
            //    print("points: \(points)")
            //}
            
            
            /*if let item = json as? NSArray{
                print("in 1")
                if let jsItem = item[0] as? NSDictionary{
                    print("in 2")
                    if let points = jsItem["numPoints"] as? NSDictionary{
                        print("in 3")
                        print("points: \(points)")
                    }
                }
            }*/
            
            
        } catch {
            print("error serializing JSON: \(error)")
        }
            i++;
        }
    
    
    
    }
    
    func editAdjListNetworkConnection(idNum : String, adjList : String){
        let addr = "moore11.cs.purdue.edu"
        let port = 3112
        
        //       var host :NSHost = NSHost(address: addr)
        var inp :NSInputStream?
        var out :NSOutputStream?
        
        NSStream.getStreamsToHostWithName(addr, port: port, inputStream: &inp, outputStream: &out)
        
        //let inputStream = inp!
        let outputStream = out!
        
        //inputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
        outputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
        
        //inputStream.open()
        outputStream.open()
        
        print("11\n")
        let dataStr = "11\n" //change to add waypoint number
        
        outputStream.write(dataStr , maxLength: 3)
        
        //json build test
        let validDictionary = [
            "id": idNum,
            "adjID": adjList
        ]
        //print("valid Dictionary \(validDictionary)")
        
        
        if NSJSONSerialization.isValidJSONObject(validDictionary) { // True
            do {
                //let rawData = try NSJSONSerialization.dataWithJSONObject(validDictionary, options: .PrettyPrinted)
                //print(rawData)
                //outputStream.write(UnsafePointer<UInt8>(rawData.bytes) , maxLength: 1024)
                //print("Waypoint Addition at - \(validDictionary)")
                let send = "\(validDictionary)\n"
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

        
    }
    
    func addWaypointNetworkConnection(adjList : String){
        let addr = "moore11.cs.purdue.edu"
        let port = 3112
        
        //       var host :NSHost = NSHost(address: addr)
        var inp :NSInputStream?
        var out :NSOutputStream?
        
        NSStream.getStreamsToHostWithName(addr, port: port, inputStream: &inp, outputStream: &out)
        
        //let inputStream = inp!
        let outputStream = out!
        
        //inputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
        outputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
        
        //inputStream.open()
        outputStream.open()
        
        print("14\n")
        let dataStr = "14\n" //change to add waypoint number
        
        outputStream.write(dataStr , maxLength: 3)
        
        //json build test
        let validDictionary = [
            "lat": "\(currentLat)",
            "longe": "\(currentLong)",
            "adjID": adjList
        ]
        //print("valid Dictionary \(validDictionary)")
        
        
        if NSJSONSerialization.isValidJSONObject(validDictionary) { // True
            do {
                //let rawData = try NSJSONSerialization.dataWithJSONObject(validDictionary, options: .PrettyPrinted)
                //print(rawData)
                //outputStream.write(UnsafePointer<UInt8>(rawData.bytes) , maxLength: 1024)
                print("Waypoint Addition at - \(validDictionary)")
                let send = "\(validDictionary)\n"
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

        //inputStream.close()
        //outputStream.close()
        
        
    }
    
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
            "lat": "\(currentLat)",
            "longe": "\(currentLong)",
        ]
        
        
        
        if NSJSONSerialization.isValidJSONObject(validDictionary) { // True
            do {
                //let rawData = try NSJSONSerialization.dataWithJSONObject(validDictionary, options: .PrettyPrinted)
                //print(rawData)
                //outputStream.write(UnsafePointer<UInt8>(rawData.bytes) , maxLength: 1024)
                print("Valid Dictionary - \(validDictionary)")
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
        
        

        //Output from server --- Commented out for testing
        /*
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
        */
        
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
        print("viewDidLoad")
        super.viewDidLoad()
        
        self.locationManager.delegate = self
        
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest
        
        self.locationManager.requestWhenInUseAuthorization()
        
        self.locationManager.startUpdatingLocation()
        
        self.mapView.showsUserLocation = true
        
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
        
        //self.locationManager.stopUpdatingLocation()
        
        let locValue:CLLocationCoordinate2D = manager.location!.coordinate
        
        print("locations =  latitude: \(locValue.latitude) longitude: \(locValue.longitude)")
        
        
        
        currentLat = "\(locValue.latitude)"
        currentLong = "\(locValue.longitude)"
    }
    
    
    func locationManager(manager: CLLocationManager, didFailWithError error: NSError)
    {
        print("Errors " + error.localizedDescription)
    }
    
}

