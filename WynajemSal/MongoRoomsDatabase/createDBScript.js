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
            description: {
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
    name: "pokoj1",
    price: NumberInt(15),
    createdDate: "2017-01-02",
    description: "opis pokoju 1"
});

db.Rooms.insert({
    name: "pokoj2",
    price: NumberInt(15),
    createdDate: "2017-01-02",
    description: "opis pokoju 2"
});

db.Rooms.insert({
    name: "pokoj3",
    price: NumberInt(30),
    createdDate: "2017-01-02",
    description: "opis pokoju 3"
});

db.Rooms.insert({
    name: "pokoj4",
    price: NumberInt(60),
    createdDate: "2017-01-02",
    description: "opis pokoju 4"
});

print('* Inserted rooms into db');

print('All collections: ' + db.getCollectionNames());
print('* Script finished.');