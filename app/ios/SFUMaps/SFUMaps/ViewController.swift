//
//  ViewController.swift
//  SFUMaps
//
//  Created by Raja Noronha on 2015-05-30.
//  Copyright (c) 2015 Bosco Noronha. All rights reserved.
//

import UIKit

class ViewController: UIViewController, CLLocationManagerDelegate {
    
    @IBOutlet weak var viewMap: GMSMapView!
    
    @IBOutlet weak var myLocation: UIButton!
    
 
    var locationManager = CLLocationManager()
    
    var didFindMyLocation = false
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        //SFU AQ location
        
        var camera = GMSCameraPosition.cameraWithLatitude(49.279014, longitude: -122.916528, zoom: 18.0)
//      var mapView = GMSMapView(frame: self.view.bounds)
            var mapView = GMSMapView.mapWithFrame(CGRectZero, camera:camera)
        self.view = mapView
//        var southWest = CLLocationCoordinate2DMake(49.272216,-122.933793)
//        var northEast = CLLocationCoordinate2DMake(49.281288,-122.902336)
//        var bounds = GMSCoordinateBounds(coordinate: northEast, coordinate: southWest)
//        var camera = mapView.cameraForBounds(bounds, insets:UIEdgeInsetsZero)
//        mapView.camera = camera;
//        
//        GMSCameraUpdate.fitBounds(bounds)
        

        
        
        //Enables mylocation
        mapView.myLocationEnabled = true
        
        // The myLocation attribute of the mapView may be null
        if let mylocation = mapView.myLocation {
            NSLog("User's location: %@", mylocation)
        } else {
            NSLog("User's location is unknown")
        }
        
        mapView.settings.compassButton = true
        
        //Set max & min zoom levels
        //mapView.setMinZoom(15, maxZoom: 21)
        
        
        var southWest = CLLocationCoordinate2DMake(49.272216,-122.933793);
        var northEast = CLLocationCoordinate2DMake(49.281288,-122.902336);
        var overlayBounds = GMSCoordinateBounds(coordinate: northEast, coordinate: southWest)
        
        // Image from http://www.lib.utexas.edu/maps/historical/newark_nj_1922.jpg
        var icon = UIImage(named: "file-page1.png")
        
        var overlay = GMSGroundOverlay(bounds: overlayBounds, icon: icon)
        overlay.bearing = 0
        overlay.map = mapView
        

    }
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    // MARK: IBAction method implementation
    
    @IBAction func changeMapType(sender: AnyObject) {

        
    }
    
    
    @IBAction func findAddress(sender: AnyObject) {
        
        
    }
    
    
    @IBAction func createRoute(sender: AnyObject) {
        
    }
    
    
    @IBAction func changeTravelMode(sender: AnyObject) {
        
    }
    

}

