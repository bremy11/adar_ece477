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
var GPSFlag : Int = Int()

class ViewController: UIViewController, MKMapViewDelegate, CLLocationManagerDelegate
{
    @IBOutlet weak var mapView: MKMapView!
    
    let locationManager = CLLocationManager()

    
    @IBAction func GPSbutton(sender: UIButton) {
        //GPSFlag = 1;
        while(true){
            sleep(1);
            /*
            var currLatitude = self.locationManager.location?.coordinate.latitude
            var currLongitude = self.locationManager.location?.coordinate.longitude
            print("\(currLatitude) , \(currLongitude)")*/
        }
        
    }

    
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
    
    
    @IBAction func ViewButton(sender: UIButton) {
        
        self.getWaypoints()
    }
    
    //BUTTONS
    @IBAction func AddButton(sender: UIButton) {
        
        
        let alert2 = UIAlertController(title: "", message: "Adding a waypoint at your current location.", preferredStyle: UIAlertControllerStyle.Alert)
        
        //alert2.addTextFieldWithConfigurationHandler({ (textField) -> Void in
        //    textField.text = "Example: 1,3"
        //})
        
        alert2.addAction(UIAlertAction(title: "OK", style: .Default, handler: { (action) -> Void in
            //let textField = alert2.textFields![0] as UITextField
            //print("Text field: \(textField.text)")
            self.addWaypointNetworkConnection()
            //self.getWaypoints()
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
                
                
            case .Cancel:
                print("cancel")
                
            case .Destructive:
                print("destructive")
            }
        }))
    }
    
    
   /* func GPSNetworkConnection(){
        
        
        let addr = "moore11.cs.purdue.edu"
        let port = 3112
        
        var inp :NSInputStream?
        var out :NSOutputStream?
        
        NSStream.getStreamsToHostWithName(addr, port: port, inputStream: &inp, outputStream: &out)
        
        let outputStream = out!
        
        outputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)

        outputStream.open()
        
        print("20\n")
        let dataStr = "20\n" //send GPS number for server
        
        outputStream.write(dataStr , maxLength: 3)
        
        
        while(true) {
            //json build test
            let validDictionary = [
                "lat": "\(currentLat)",
                "longe": "\(currentLong)"
                //"adjID": adjList
            ]
            
            sleep(1)
            //print("Sending GPS location - \(validDictionary)")
            
            if NSJSONSerialization.isValidJSONObject(validDictionary) { // True
                do {
                    
                    print("Sending GPS location - \(validDictionary)")
                    let send = "\(validDictionary)\n"
                    outputStream.write(send, maxLength: send.characters.count);
                    //let newLine = "\n"
                    //outputStream.write(newLine , maxLength: 1)
                    
                } catch {
                    // Handle Error
                }
                
            }else{
                print("Invalid JSON data")
                
            }
        }
        
    }*/
    
    
    
    func displayDelivery(Deliverylat: Double, Deliverylong: Double)
    {
        let waypoint = CLLocationCoordinate2DMake(Deliverylat, Deliverylong)
        let pinDrop = MKPointAnnotation()
        pinDrop.coordinate = waypoint
        pinDrop.title = "Delivery Drop-Off"
        mapView.addAnnotation(pinDrop)
    }
    

    
    func displayWaypoints( lat: Double, long: Double, num: Int) {
        
        let waypoint = CLLocationCoordinate2DMake(lat, long)
        let pinDrop = MKPointAnnotation()
        pinDrop.coordinate = waypoint
        
        pinDrop.title = "Waypoint ID : \(num)"
        pinDrop.subtitle = "\(lat) , \(long)"
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
        
        var i = 1;
        var numPoints = 1;
        
        while ( i <= numPoints)
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
            let strnumPoints = String(boardsDictionary["numPoints"]!)
            let pinLatString = String(boardsDictionary["lat"]!)
            let pinLongString = String(boardsDictionary["longe"]!)
            let pinNumString = String(boardsDictionary["id"]!)
            
            numPoints =  Int(strnumPoints)!
            let pinLat = Double(pinLatString)
            let pinLong = Double(pinLongString)
            let pinNum = Int(pinNumString)
            
            //print("pinLat: \(pinLat) , pinLong: \(pinLong) , pinNum: \(pinNum)")
            self.displayWaypoints(pinLat!, long: pinLong!, num: pinNum!)
            
            
            
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
    
    
    func addWaypointNetworkConnection(){
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
            "longe": "\(currentLong)"
            //"adjID": adjList
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
        
        let dataStr = "13\n"

        outputStream.write(dataStr , maxLength: 3)
        
        //json build test
        /* //INFORMATION RECEIVED FROM SERVER
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
        print("outStr: \(outStr)")
        
        if(outStr != "Error, no available delivery rover\n"){
            print("AVAILABLE ROVER")
            
            do {
                
                let data: NSData = outStr.dataUsingEncoding(NSUTF8StringEncoding, allowLossyConversion: false)!
                //let json = try NSJSONSerialization.JSONObjectWithData(outStr, options: [])
                let boardsDictionary: NSDictionary = try NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers) as! NSDictionary
                
                //let json = try NSJSONSerialization.JSONObjectWithData(data, options: .AllowFragments)
                print(boardsDictionary)
                print(boardsDictionary["numPoints"]!)
                let pinLatString = String(boardsDictionary["lat"]!)
                let pinLongString = String(boardsDictionary["longe"]!)
                
                let pinLat = Double(pinLatString)
                let pinLong = Double(pinLongString)
                
                //print("pinLat: \(pinLat) , pinLong: \(pinLong) , pinNum: \(pinNum)")
                self.displayDelivery(pinLat!, Deliverylong: pinLong!)
                
                
                
            } catch {
                print("error serializing JSON: \(error)")
            }
            
            self.successfulOrder()

        }
        else
        {
            print("NO ROVER AVAIL")
            
            self.unsuccessfulOrder()
        }*/ //end of input from server
        
        
        
        
        
        
        /*
        self.dropAPin()
        
        self.successfulOrder()
        
        do {
                
            let data: NSData = outStr.dataUsingEncoding(NSUTF8StringEncoding, allowLossyConversion: false)!
                //let json = try NSJSONSerialization.JSONObjectWithData(outStr, options: [])
            let boardsDictionary: NSDictionary = try NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers) as! NSDictionary
                
                //let json = try NSJSONSerialization.JSONObjectWithData(data, options: .AllowFragments)
            print(boardsDictionary)
            print(boardsDictionary["numPoints"]!)
            let pinLatString = String(boardsDictionary["lat"]!)
            let pinLongString = String(boardsDictionary["longe"]!)
            
            let pinLat = Double(pinLatString)
            let pinLong = Double(pinLongString)
                
                //print("pinLat: \(pinLat) , pinLong: \(pinLong) , pinNum: \(pinNum)")
            self.displayDelivery(pinLat!, Deliverylong: pinLong!)
            
                
                
            } catch {
                print("error serializing JSON: \(error)")
            }
        */
    
    
    }
    
    func unsuccessfulOrder(){
        
        let alert = UIAlertController(title: "", message: "There are no current rovers available for delivery. Please try again later.", preferredStyle: UIAlertControllerStyle.Alert)
        
        alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default, handler: nil))
        
        self.presentViewController(alert, animated: true, completion: nil)
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
        
        //print("locations =  latitude: \(locValue.latitude) longitude: \(locValue.longitude)")
        
        
        
        currentLat = "\(locValue.latitude)"
        currentLong = "\(locValue.longitude)"
        
        //print("\(currentLat) , \(currentLong)")
        
        
        
        //SEND GPS LOCATION TO SERVER
        if(GPSFlag == 1){
            
            
                //json build test
                let validDictionary = [
                    "lat": "\(currentLat)",
                    "longe": "\(currentLong)"
                    //"adjID": adjList
                ]
                
                //sleep(1)
                print("Sending GPS location - \(validDictionary)")
            
        }
        /*
            
        
            let addr = "moore11.cs.purdue.edu"
            let port = 3112
            
            var inp :NSInputStream?
            var out :NSOutputStream?
            
            NSStream.getStreamsToHostWithName(addr, port: port, inputStream: &inp, outputStream: &out)
            
            let outputStream = out!
            
            outputStream.scheduleInRunLoop(.mainRunLoop(), forMode: NSDefaultRunLoopMode)
            
            outputStream.open()
            
            print("20\n")
            let dataStr = "20\n" //send GPS number for server
            
            outputStream.write(dataStr , maxLength: 3)
            
            
            while(true) {
                //json build test
                let validDictionary = [
                    "lat": "\(currentLat)",
                    "longe": "\(currentLong)"
                    //"adjID": adjList
                ]
                
                sleep(1)
                //print("Sending GPS location - \(validDictionary)")
                //}
                
                if NSJSONSerialization.isValidJSONObject(validDictionary) { // True
                    do {
                        
                        print("Sending GPS location - \(validDictionary)")
                        let send = "\(validDictionary)\n"
                        outputStream.write(send, maxLength: send.characters.count);
                        //let newLine = "\n"
                        //outputStream.write(newLine , maxLength: 1)
                        
                    } catch {
                        // Handle Error
                    }
                    
                }else{
                    print("Invalid JSON data")
                    
                }
            }
            
            
        }
        
    }
    
    
    func locationManager(manager: CLLocationManager, didFailWithError error: NSError)
    {
        print("Errors " + error.localizedDescription)
    }*/
    
    }
}