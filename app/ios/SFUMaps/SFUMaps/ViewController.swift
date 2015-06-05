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
        super.viewDidLoad()
        var camera = GMSCameraPosition.cameraWithLatitude(49.279014, longitude: -122.916528, zoom: 18.0)
        
        
        var mapView = GMSMapView.mapWithFrame(CGRectZero, camera:camera)
        self.view = mapView
        
        //Set max & min zoom levels
        mapView.setMinZoom(15, maxZoom: 21)
        
//        //Creates view range coordinates
//        let left = CLLocationCoordinate2DMake(49.278954, -122.937197)
//        let right = CLLocationCoordinate2DMake(49.278230, -122.901845)
//        
//        //Sets view range coordinates
//        var bounds = GMSCoordinateBounds(coordinate: left, coordinate: right)
    
        
        var southWest = CLLocationCoordinate2DMake(449.273319,-122.934577);
        var northEast = CLLocationCoordinate2DMake(49.280993,-122.901603);
        var overlayBounds = GMSCoordinateBounds(coordinate: southWest, coordinate: northEast)
        
        // Image from sfu site
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

