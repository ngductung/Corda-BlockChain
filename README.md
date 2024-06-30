# Corda Booking Red-light District

## Cài đặt
- Cài JDK 1.8.0_411 tại [đây](https://www.oracle.com/java/technologies/downloads/?er=221886#java8-windows).
    - Setting enviroment java đến đường dẫn của jdk-1.8/bin luôn như như này.
        ![img](https://raw.githubusercontent.com/ngductung/Corda-BlockChain/main/img/enviroment.png)
    -  Kiểm tra lại version
        ![img](https://raw.githubusercontent.com/ngductung/Corda-BlockChain/main/img/version.png)
- Tải source code tại [đây](https://github.com/ngductung/Corda-BlockChain/tree/main/Booking).


## Build
- Truy cập vào folder source code và chạy lệnh để deploy nodes:
    ```powershell
        .\gradlew.bat deployNodes
    ```
- Truy cập vào folder `build\nodes` và chạy file `runnodes.bat`, kết quả sẽ tạo ra 3 processes: `Notary`, `VIPClient` và `RedLightDistrict`.

## Tương tác
- Kiểm tra state:
    ```powershell
    run vaultQuery contractStateType: com.template.states.BookingState
    ```
- List flow
    ```powershell
    flow list
    ```
- Đặt phòng
    ```powershell
    flow start Initiator hotelName: "hehe", roomNumber: 666, checkInDate: "2024-06-28", checkOutDate: "2024-06-30", guestName: "hihi", guestEmail: "test@email.com", guestAge: 19, roomType: "K", originalRoomPrice: 100, creditCardNumber: "1234567890123456", creditCardExpiryDate: "2024-10-30", bookingReference: "xxx", counterparty: "O=RedLightDistrict,L=AT1705,C=JP"
    ```
