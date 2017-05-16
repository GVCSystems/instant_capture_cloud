package com.arpit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;

@Document(collection = "webcam")
public class MyBean
 {

     public String year,month,date,hour,min,sec,file;
     public MyBean(){}

     public MyBean(String year,String month,String date,String hour,String min,String sec,String file)
     {
         this.year  = year;
         this.month = month;
         this.date = date;
         this.hour = hour;
         this.min = min;
         this.sec = sec;
         this.file = file;
     }

     public MyBean(File file)
     {
         StringBuilder builder = new StringBuilder(file.getName());

         this.year = builder.substring(0,builder.indexOf("-"));
         builder.delete(0,builder.indexOf("-")+1);

         this.month = builder.substring(0,builder.indexOf("-"));
         builder.delete(0,builder.indexOf("-")+1);

         this.date = builder.substring(0,builder.indexOf("-"));
         builder.delete(0,builder.indexOf("-")+1);

         this.hour = builder.substring(0,builder.indexOf("-"));
         builder.delete(0,builder.indexOf("-")+1);

         this.min = builder.substring(0,builder.indexOf("-"));
         builder.delete(0,builder.indexOf("-")+1);

         this.sec = builder.substring(0,builder.indexOf("."));
         builder.delete(0,builder.indexOf(".")+1);

         this.file = file.getName();
     }

 }