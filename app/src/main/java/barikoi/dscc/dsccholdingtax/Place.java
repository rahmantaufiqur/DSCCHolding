package barikoi.dscc.dsccholdingtax;

import java.io.Serializable;

/**
 * This class contains the info of a place including code, city, area
 * We use this Class all over the project to set and get a place
 * Created by Sakib on 04-Jan-17.
 */
public class Place implements Serializable {
    private String holdingNo,holdingDesc,lon,lat,road,block,sector,section,subarea,area,postalcode,ward,zone;
    public Place(){}

    public Place(String holdingNo, String holdingDesc, String lon, String lat, String road, String block, String sector, String section, String subarea, String area, String postalcode, String ward, String zone) {
        this.holdingNo = holdingNo;
        this.holdingDesc = holdingDesc;
        this.lon = lon;
        this.lat = lat;
        this.road = road;
        this.block = block;
        this.sector = sector;
        this.section = section;
        this.subarea = subarea;
        this.area = area;
        this.postalcode = postalcode;
        this.ward = ward;
        this.zone = zone;
    }

    public String getHoldingNo() {
        return holdingNo;
    }

    public String getHoldingDesc() {
        return holdingDesc;
    }

    public String getLon() {
        return lon;
    }

    public String getLat() {
        return lat;
    }

    public String getRoad() {
        return road;
    }

    public String getBlock() {
        return block;
    }

    public String getSector() {
        return sector;
    }

    public String getSection() {
        return section;
    }

    public String getSubarea() {
        return subarea;
    }

    public String getArea() {
        return area;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public String getWard() {
        return ward;
    }

    public String getZone() {
        return zone;
    }

    @Override
    public String toString() {
        String str="";
        return str;
    }


}
