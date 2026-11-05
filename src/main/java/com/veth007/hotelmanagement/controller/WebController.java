// package com.veth007.hotelmanagement.controller;

// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.ResponseBody;

// @Controller
// @RequestMapping("/paypal")
// public class WebController {
//    // @PreAuthorize("isAuthenticated() and  hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF')")
//     @GetMapping("/")
//     public String home() {
//         return "payment";
//     }

//   //   @PreAuthorize("isAuthenticated() and  hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF')")
//     @GetMapping("/book")
//     public String book() {
//         return "book";
//     }

//    // @PreAuthorize("isAuthenticated() and  hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF')")
//     @GetMapping("/unpaid")
//     public String unpaid() {
//         return "unpaid";
//     }

//     /*
//      * @GetMapping("/success")
//      * public String success (){
//      * return "payment successful";
//      * 
//      * }
//      */

//     @PreAuthorize("isAuthenticated() and  hasAnyRole('ADMIN')")
//     @GetMapping("/cancel")
//     @ResponseBody
//     public String cancel() {
//         return "payment canceled";
//     }
// }
