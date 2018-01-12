var db = connect('127.0.0.1:27017/BookingsRoomDB');
print('* Connected to database');

db.dropDatabase();

db.Rooms.drop();
db.createCollection("Rooms", {
    validator: {
        $and: [{
            name: {
                $type: "string",
                $exists: true
            },
            price: {
                $type: "int",
                $exists: true
            },

            capacity: {
                $type: "int",
                $exists: true
            },

            description: {
                $type: "string",
                $exists: true
            },

            "address.street": {
                $type: "string",
                $exists: true
            },
            "address.zipcode": {
                $type: "string",
                $exists: true
            },
            "address.venueNumber": {
                $type: "string",
                $exists: true
            },
            "address.city": {
                $type: "string",
                $exists: true
            },

            type: {
                $type: "string",
                $exists: true
            },

            createdDate: {
                $type: "string",
                $exists: true
            }
        }]
    },
    validationAction: "error"
});
db.Rooms.createIndex(
    {name: 1},
    {unique: true}
);
print('* Created collection Rooms. All collections: ' + db.getCollectionNames());

db.dropRole("RoomsDatabaseProxyRole");
db.createRole({
    role: "RoomsDatabaseProxyRole",
    privileges: [{
        resource: {
            db: "BookingsRoomDB",
            collection: "Rooms"
        },
        actions: [ "find", "update", "insert", "remove" ]
    }],
    roles: []
});
print('* Created role RoomsDatabaseProxyRole');

db.dropUser("RoomsDatabaseProxyUser");
db.createUser({
    user: "RoomsDatabaseProxyUser",
    pwd: "BookingsRoomp@Ssw0rd",
    roles: [{
        role: "RoomsDatabaseProxyRole",
        db: "BookingsRoomDB"
    }]
});
print('* Created user RoomsDatabaseProxyUser');

db.Rooms.insert({
    name: "Sala 1A",
    price: NumberInt(300),
    capacity: NumberInt(50),
    createdDate: "2017-01-02",
    type: "Konferencyjna",
    description: "Opis sali 1",
    address: {
        street: "ul. Tymienieckiego",
        venueNumber: "25a",
        zipcode: "90-350",
        city: "Łódź"
    }
});

db.Rooms.insert({
    name: "Sala 2A",
    price: NumberInt(450),
    capacity: NumberInt(60),
    createdDate: "2017-01-02",
    type: "Specjalistyczna",
    description: "Opis sali 2",
    address: {
        street: "ul. Tymienieckiego",
        venueNumber: "25c",
        zipcode: "90-350",
        city: "Łódź"
    }
});

db.Rooms.insert({
    name: "Sala 3",
    price: NumberInt(500),
    capacity: NumberInt(75),
    createdDate: "2017-01-02",
    type: "Bankietowa",
    description: "Opis sali 3",
    address: {
    street: "ul. Tymienieckiego",
        venueNumber: "25d",
        zipcode: "90-350",
        city: "Łódź"
}});

db.Rooms.insert({
    name: "Sala 4",
    price: NumberInt(250),
    capacity: NumberInt(50),
    createdDate: "2017-01-02",
    type: "Bankietowa",
    description: "Opis sali 4",
    address: {
    street: "ul. Tymienieckiego",
        venueNumber: "20",
        zipcode: "90-350",
        city: "Łódź"
}});

print('* Inserted rooms into db');

print('All collections: ' + db.getCollectionNames());
print('* Script finished.');