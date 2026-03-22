package com.transylvania.config;

import com.transylvania.model.AddOn;
import com.transylvania.model.Room;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookingRequest {

    // dates & guests
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int adults   = 1;
    private int children = 0;

    // select room
    private Room selectedRoom;

    // guest info
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // add-ons
    private final Map<AddOn, Integer> selectedAddOns = new LinkedHashMap<>();
    public long getNights() {
        if (checkIn == null || checkOut == null) return 0;
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public int getTotalGuests() {
        return adults + children;
    }

    public double getRoomTotal() {
        if (selectedRoom == null || getNights() == 0) return 0;
        return selectedRoom.getRoomType().getBasePrice() * getNights();
    }

    public double getAddOnsTotal() {
        double total = 0;
        for (Map.Entry<AddOn, Integer> entry : selectedAddOns.entrySet()) {
            AddOn addOn   = entry.getKey();
            int  qty      = entry.getValue();
            double unit   = addOn.getPrice() * qty;
            if ("PER_NIGHT".equalsIgnoreCase(addOn.getPricingType())) {
                unit *= getNights();
            }
            total += unit;
        }
        return total;
    }

    public double getGrandTotal() {
        return getRoomTotal() + getAddOnsTotal();
    }

    public String getGuestFullName() {
        return firstName + " " + lastName;
    }

    // add on helpers
    public void setAddOnQuantity(AddOn addOn, int qty) {
        if (qty <= 0) {
            selectedAddOns.remove(addOn);
        } else {
            selectedAddOns.put(addOn, qty);
        }
    }

    public boolean hasAddOn(AddOn addOn) {
        return selectedAddOns.containsKey(addOn);
    }

    public LocalDate getCheckIn()                   { return checkIn; }
    public void setCheckIn(LocalDate v)             { this.checkIn = v; }

    public LocalDate getCheckOut()                  { return checkOut; }
    public void setCheckOut(LocalDate v)            { this.checkOut = v; }

    public int getAdults()                          { return adults; }
    public void setAdults(int v)                    { this.adults = v; }

    public int getChildren()                        { return children; }
    public void setChildren(int v)                  { this.children = v; }

    public Room getSelectedRoom()                   { return selectedRoom; }
    public void setSelectedRoom(Room v)             { this.selectedRoom = v; }

    public String getFirstName()                    { return firstName; }
    public void setFirstName(String v)              { this.firstName = v; }

    public String getLastName()                     { return lastName; }
    public void setLastName(String v)               { this.lastName = v; }

    public String getEmail()                        { return email; }
    public void setEmail(String v)                  { this.email = v; }

    public String getPhone()                        { return phone; }
    public void setPhone(String v)                  { this.phone = v; }

    private boolean loyaltyEnroll = false;
    public boolean isLoyaltyEnroll()              { return loyaltyEnroll; }
    public void setLoyaltyEnroll(boolean v)       { this.loyaltyEnroll = v; }

    public Map<AddOn, Integer> getSelectedAddOns()  { return selectedAddOns; }
}
