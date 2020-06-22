/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firstScan;

import com.sun.org.apache.bcel.internal.generic.AALOAD;
import com.sun.xml.internal.ws.api.message.saaj.SAAJFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ErrorMessages;
import marie.MainScreen;

/**
 *
 * @author user
 */
public class FirstScan {
    
    public String decToHex(int decimal){
        String hexadecimal = Integer.toHexString(decimal);
        hexadecimal = hexadecimalCreator(hexadecimal);
        return hexadecimal;
    }
    
    public String binToHEx(String bin){
        int decimal = Integer.parseInt(bin,2);
        String hexa = Integer.toString(decimal,16);
        
        return hexa;
    }
    
    public String hexToBin(String hex){
        hex = hex.replaceAll("0", "0000");
        hex = hex.replaceAll("1", "0001");
        hex = hex.replaceAll("2", "0010");
        hex = hex.replaceAll("3", "0011");
        hex = hex.replaceAll("4", "0100");
        hex = hex.replaceAll("5", "0101");
        hex = hex.replaceAll("6", "0110");
        hex = hex.replaceAll("7", "0111");
        hex = hex.replaceAll("8", "1000");
        hex = hex.replaceAll("9", "1001");
        hex = hex.replaceAll("A", "1010");
        hex = hex.replaceAll("B", "1011");
        hex = hex.replaceAll("C", "1100");
        hex = hex.replaceAll("D", "1101");
        hex = hex.replaceAll("E", "1110");
        hex = hex.replaceAll("F", "1111");
        return hex;
    }
    public String hexadecimalCreator(String hexa){
        /*
        Bu method hexadecimal sayının boyunutunu 4 bit olarak ayarlıyo.
        */
         if(hexa.length() < 4){
            while(hexa.length()!= 4){
                hexa = "0" +hexa;
            }
        } else if(hexa.length() > 4){
            hexa = hexa.substring(hexa.length()-4);
        }
        return hexa;
    }
    
    public Map<String,String> addressInserter(List<String> codeBlock){
        /*
        Bu method bellek tablosunu doldurmak için var.
        */
        Map<String,String> codesPyhsicalAddresses = new HashMap<>();
        
        Map<String,String> codesAddress = codeAddressMatching(codeBlock);
        Map<String,String> tagValues = tagValueCalculator(codeBlock);
        Map<String,String> catchedTag = tagCatcher(codeBlock);
        Map<String,String> symbols = readSymbolText();
        
        Object[] codes = codesAddress.values().toArray();
        Object[] location = codesAddress.keySet().toArray();
        
        for (int i= 0; i< codes.length; i++){
            String[] line = codes[i].toString().split(" ");
            if(line.length <2){
                String adrs = symbols.get(line[0].toString());//opcode
                if(adrs!= null){
                    /* burada hexadecimal olan adres binary code olarak çevrildi ve opcode 
                    ile eklenerek tekrardan hexadecimal oldu ve map objesine eklendi*/
                    adrs = hexToBin(adrs);//bin opcode
                    String y = location[i].toString();//hexa address
                    y = hexToBin(y);//binary address;
                    adrs = adrs + y;
                    adrs = binToHEx(adrs);
                    codesPyhsicalAddresses.put(location[i].toString(),adrs);
                }
            }
            else if (line.length ==2){
                String sym = symbols.get(line[0]);//başlangıç değeri içerik
                if(!sym.equals(null)){
                    String addrs = catchedTag.get(line[1]);
                    addrs = hexToBin(addrs);
                    String adr = sym + addrs;
                    adr = binToHEx(adr);
                    codesPyhsicalAddresses.put(location[i].toString(),adr);
                }
            } else{
                String val = tagValues.get(location[i]);//etiketin hexadecimal değeri
                codesPyhsicalAddresses.put(location[i].toString(),val);
            }
        }
        return codesPyhsicalAddresses;
    }
    
    public Map<String,String> tagValueCalculator(List<String> codeBlock){
        //*BU method koddaki etiketlerin değerlerini hesaplayıp fiziksel adresleriyle beraber hashmap objesinin içinde tutar.
        Map<String,String> tagValues = new HashMap<String,String>();//<Adres,Hex Değeri>
        List<String> memory = memoryAddress(codeBlock);
        Map<String,String> tags = tagCatcher(codeBlock);//<Etiket Adı,Adres>
        try{
            for(int i= 0; i< codeBlock.size()-1; i++){
               String[] splittedIndex = codeBlock.get(i+1).split(", ");
               if(splittedIndex.length >= 2){
                   String tagAddress = memory.get(i);
                   String[] code = splittedIndex[1].split(" ");
                   code[0] = code[0].toLowerCase();
                   if(code[0].equals("dec")){
                       int decimal = Integer.parseInt(code[1]);
                       String hexa = decToHex(decimal);
                       tagValues.put(tagAddress,hexa);
                   } else if(code[0].equals("hex")){
                       tagValues.put(tagAddress, code[1]);
                   }
               }
                
            }
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return tagValues;
    }
    
    public Map<String,String> codeAddressMatching(List<String> codeBlock){
        /*
        her bir kod satırını adreslerle eşleştirip HashMap in içinde tutuyor.
        */
        Map<String,String> addresses = new HashMap<String,String>();//adreslerin ve kodun karşılıklı tutulacağı map objesi
        List<String> codes = new ArrayList<String>();//kodların tutulacağı arraylist
        List<String> locations = memoryAddress(codeBlock);//bellekteki adreslerin değerleri
        for(int i= 1; i< codeBlock.size(); i++){
            codes.add(codeBlock.get(i));//yazılan kodu satır satır listeye atama yapıyo
        }
        for(int i= 0; i< codes.size(); i++){
            addresses.put(locations.get(i), codes.get(i));//adreslerle kodları karşılılı map objesine atıyo
        }
        
        return addresses;
    }
    
    public List<String> codeSeperator(List<String> codeBlock){
        /*
        yazılan kodu org komutundan başlayıp end komutuna kadar alıp bir listeye atayıp o listeyi döndürüyo
        tabi koddaki end satırını listeye dahil etmiyor
        */
       List<String> newCode = new ArrayList<String>();
        for(int i= 0; i< codeBlock.size(); i++){
            if(codeBlock.get(i).toLowerCase().startsWith("end")){
                break;
            } else {
                newCode.add(codeBlock.get(i));
            }
        }
        return newCode;
    }
    
    public Map<String,String> tagCatcher(List<String> codeBlock){
        /* 
        bu method koddaki etiketleri yakalayıp onların hexadecimal adres 
        değerlerini bir string e string hashmap içinde tutuyor
        bu method önyüzdeki etiket tablosunu doldurmak için kullanılacak
        */
        Map<String,String> tags = new HashMap<String,String>();
        List<String> memory = memoryAddress(codeBlock);
        
        for (int i= 0; i< codeBlock.size()-1; i++){
            String[] splittedIndex = codeBlock.get(i+1).split(",");
            if(splittedIndex.length >= 2){
                String tagAddress = memory.get(i);
                tags.put(splittedIndex[0],tagAddress);
            }
        }
        return tags;
    }
    
    public List<String> memoryAddress(List<String> codeBlock){
        /*
        bu method text area dan girilen kodu ayrıştırıp ORG komutundan END komutuna kadar olan 
        adres değerlerini hexadecimal olarak bir listede tutuyor,
        */
        List<String> memoryAddresses = new ArrayList<>();
        
        String[] memoryStart = codeBlock.get(0).toLowerCase().split("org ");
        int decimal = Integer.parseInt(memoryStart[1], 16);
        
        for(int i= 1; i< codeBlock.size(); i++){
            String hexadecimal = Integer.toHexString(decimal);
            memoryAddresses.add(hexadecimal);
            decimal++;
        }
        return memoryAddresses;
    }
    public Object[][] mapToArrayConverter(Map<String,String> tags){
        /*
        verileri ekrandaki listeye yazdırmak için map objesini iki boyutlu bir diziye çevirir
        */
        Object[][] convertedMap = new String[tags.size()][2];
        
        Object[] tag = tags.keySet().toArray();
        Object[] address = tags.values().toArray();
        
        for (int i= 0; i< convertedMap.length; i++){
            convertedMap[i][0] = tag[i];
            convertedMap[i][1] = address[i];
        }
        
        return convertedMap;
    }
    
    
    
    public Map<String,String> readSymbolText(){
        /*
        Bu method symbolAndHex.txt dosyasındaki semboller ve karşılık gelen 
        hexadecimal sayıları bir hashmap içine taşıyıp 
        programın içinde daha rahat taşınılmasını sağlıyor.
        */
        Map<String,String> symbol = new HashMap<String,String>();
        String line;
        String[] parts;
        try{
            FileReader file = new FileReader("/repos/marieSimulator/symbolsAndHexCodes/symbolAndHex.txt");
            BufferedReader reader = new BufferedReader(file);
            line = reader.readLine();
            parts = line.split(",");
            for (int i= 0; i< parts.length; i++){
                String[] sym = parts[i].split("->");
                symbol.put(sym[0],sym[1]);
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return symbol;
    }
} 

