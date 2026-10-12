// In-memory mock database matching schema and 02_dummy_data.sql
let restaurants = [
  {
    restaurantId: 1,
    restaurantName: 'Phở Hà Nội',
    email: 'pho.hanoi@example.com',
    phoneNumber: '0901111111',
    address: '12 Hàng Buồm, Hoàn Kiếm, Hà Nội',
    latitude: 21.0340,
    longitude: 105.8490,
    active: true,
  },
  {
    restaurantId: 2,
    restaurantName: 'Bún Bò Huế Ngon',
    email: 'bunbo.hue@example.com',
    phoneNumber: '0902222222',
    address: '88 Lê Lợi, Hải Châu, Đà Nẵng',
    latitude: 16.0678,
    longitude: 108.2208,
    active: true,
  },
  {
    restaurantId: 3,
    restaurantName: 'Pizza Saigon',
    email: 'pizza.saigon@example.com',
    phoneNumber: '0903333333',
    address: '45 Nguyễn Huệ, Quận 1, TP.HCM',
    latitude: 10.7769,
    longitude: 106.7009,
    active: true,
  },
];

let foodItems = [
  { itemId: 1, restaurantId: 1, itemName: 'Phở bò tái', categoryName: 'Phở', price: 55000, isAvailable: true, description: 'Phở bò tươi ngon, nước dùng ninh từ xương ống truyền thống' },
  { itemId: 2, restaurantId: 1, itemName: 'Phở gà ta', categoryName: 'Phở', price: 50000, isAvailable: true, description: 'Thịt gà ta thả vườn dai ngọt, lá chanh thơm nức' },
  { itemId: 3, restaurantId: 1, itemName: 'Bún chả Hà Nội', categoryName: 'Bún', price: 60000, isAvailable: true, description: 'Chả nướng than hoa thơm lừng kèm nem rán giòn rụm' },

  { itemId: 4, restaurantId: 2, itemName: 'Bún bò Huế đặc biệt', categoryName: 'Bún', price: 65000, isAvailable: true, description: 'Đầy đủ bắp bò, giò heo, chả cua, tiết, nước dùng chuẩn vị cố đô' },
  { itemId: 5, restaurantId: 2, itemName: 'Bún bò Huế thường', categoryName: 'Bún', price: 55000, isAvailable: true, description: 'Nước dùng đậm đà sả ớt ruốc, thịt bò mềm thơm' },
  { itemId: 6, restaurantId: 2, itemName: 'Nem lụi nướng', categoryName: 'Món phụ', price: 30000, isAvailable: true, description: 'Nem lụi nướng cuốn bánh tráng rau sống, chấm nước lèo béo bùi' },

  { itemId: 7, restaurantId: 3, itemName: 'Pizza Margherita (M)', categoryName: 'Pizza', price: 120000, isAvailable: true, description: 'Sốt cà chua Ý, phô mai Mozzarella tan chảy và lá húng tây' },
  { itemId: 8, restaurantId: 3, itemName: 'Pizza BBQ Chicken (L)', categoryName: 'Pizza', price: 180000, isAvailable: true, description: 'Thịt gà nướng sốt BBQ, ớt chuông, hành tây, phô mai hảo hạng' },
  { itemId: 9, restaurantId: 3, itemName: 'Pasta Carbonara', categoryName: 'Pasta', price: 95000, isAvailable: true, description: 'Mì Ý sốt kem trứng béo ngậy cùng thịt xông khói áp chảo giòn' },
];

let paymentMethods = [
  { methodId: 1, methodCode: 'COD', methodName: 'Tiền mặt khi nhận hàng' },
  { methodId: 2, methodCode: 'VIETQR', methodName: 'Chuyển khoản VietQR' },
];

let drivers = [
  {
    driverId: 1,
    fullName: 'Phạm Đức Hùng',
    email: 'hung.pham@example.com',
    phoneNumber: '0944444444',
    licensePlate: '30A-12345',
    isOnline: true,
    isBusy: false,
    currentLatitude: 21.0285,
    currentLongitude: 105.8542,
  },
  {
    driverId: 2,
    fullName: 'Võ Thanh Tùng',
    email: 'tuan.driver@example.com',
    phoneNumber: '0955555555',
    licensePlate: '43B-67890',
    isOnline: true,
    isBusy: false,
    currentLatitude: 20.9806,
    currentLongitude: 105.7876,
  },
];

let customers = [
  {
    customerId: 1,
    fullName: 'Nguyễn Văn An',
    email: 'an.nguyen@example.com',
    phoneNumber: '0911111111',
    address: '5 Tràng Tiền, Hoàn Kiếm, Hà Nội',
  },
];

let nextOrderId = 101;
let orders = [
  {
    orderId: 100,
    customerId: 1,
    customerName: 'Nguyễn Văn An',
    restaurantId: 1,
    restaurantName: 'Phở Hà Nội',
    distanceKm: 2.5,
    shippingFee: 17500,
    totalAmount: 127500,
    orderStatus: 'DELIVERING',
    deliveryAddress: '5 Tràng Tiền, Hoàn Kiếm, Hà Nội',
    deliveryLatitude: 21.0245,
    deliveryLongitude: 105.8560,
    notes: 'Giao nhanh giúp mình',
    createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
    items: [
      { itemId: 1, itemName: 'Phở bò tái', price: 55000, quantity: 2 },
    ],
    payment: {
      paymentId: 10,
      orderId: 100,
      methodCode: 'VIETQR',
      paymentStatus: 'PAID',
      amount: 127500,
      transactionCode: 'TXN-00100',
    },
  },
];

let deliveries = [
  {
    deliveryId: 1,
    orderId: 100,
    driverId: 2,
    deliveryStatus: 'PICKED_UP',
    pickupTime: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
  },
];

let ratings = [];

function parseBody(req) {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', (chunk) => { body += chunk; });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch {
        resolve({});
      }
    });
  });
}

function sendJson(res, statusCode, data) {
  res.statusCode = statusCode;
  res.setHeader('Content-Type', 'application/json');
  res.end(JSON.stringify(data));
}

// Haversine formula calculation
function calcShipping(lat1, lon1, lat2, lon2) {
  const R = 6371; // km
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = Math.round(R * c * 10) / 10;

  const baseFee = 15000;
  const baseKm = 2.0;
  const perKmFee = 5000;
  const maxFee = 50000;

  let fee = baseFee;
  if (distance > baseKm) {
    fee += Math.ceil(distance - baseKm) * perKmFee;
  }
  fee = Math.min(fee, maxFee);

  return { distanceKm: distance, shippingFee: fee };
}

export function mockApiPlugin() {
  return {
    name: 'mock-api-middleware',
    configureServer(server) {
      server.middlewares.use(async (req, res, next) => {
        const url = req.url.split('?')[0];
        const queryParams = new URLSearchParams(req.url.split('?')[1] || '');

        if (!url.startsWith('/api/')) {
          return next();
        }

        // 1. Auth routes
        if (url === '/api/auth/customer/login' && req.method === 'POST') {
          const body = await parseBody(req);
          const cust = customers.find((c) => c.email === body.email) || customers[0];
          return sendJson(res, 200, {
            id: cust.customerId,
            name: cust.fullName,
            email: cust.email,
            role: 'CUSTOMER',
          });
        }

        if (url === '/api/auth/restaurant/login' && req.method === 'POST') {
          const body = await parseBody(req);
          const rest = restaurants.find((r) => r.email === body.email) || restaurants[0];
          return sendJson(res, 200, {
            id: rest.restaurantId,
            name: rest.restaurantName,
            email: rest.email,
            role: 'RESTAURANT',
          });
        }

        if (url === '/api/auth/driver/login' && req.method === 'POST') {
          const body = await parseBody(req);
          const drv = drivers.find((d) => d.email === body.email) || drivers[0];
          return sendJson(res, 200, {
            id: drv.driverId,
            name: drv.fullName,
            email: drv.email,
            role: 'DRIVER',
          });
        }

        if (url === '/api/auth/customer/register' && req.method === 'POST') {
          const body = await parseBody(req);
          const newCust = {
            customerId: customers.length + 1,
            fullName: body.fullName || 'Khách hàng mới',
            email: body.email,
            phoneNumber: body.phoneNumber || '0900000000',
            address: body.address || 'Hà Nội',
          };
          customers.push(newCust);
          return sendJson(res, 201, {
            id: newCust.customerId,
            name: newCust.fullName,
            email: newCust.email,
            role: 'CUSTOMER',
          });
        }

        if (url === '/api/auth/restaurant/register' && req.method === 'POST') {
          const body = await parseBody(req);
          const newRestId = restaurants.length + 1;
          const newRest = {
            restaurantId: newRestId,
            restaurantName: body.restaurantName || 'Nhà hàng mới',
            email: body.email,
            phoneNumber: body.phoneNumber || '0901234567',
            address: body.address || 'Hà Nội',
            latitude: Number(body.latitude) || 21.0285,
            longitude: Number(body.longitude) || 105.8542,
            active: true,
          };
          restaurants.push(newRest);
          // Thêm 2 món mẫu cho nhà hàng mới đăng ký để có thực đơn ngay
          foodItems.push(
            { itemId: foodItems.length + 1, restaurantId: newRestId, itemName: 'Món đặc sản 1', categoryName: 'Món chính', price: 60000, isAvailable: true, description: 'Món ăn đặc sắc của quán vừa đăng ký' },
            { itemId: foodItems.length + 2, restaurantId: newRestId, itemName: 'Nước uống giải khát', categoryName: 'Đồ uống', price: 20000, isAvailable: true, description: 'Nước ngọt giải khát mát lạnh' }
          );
          return sendJson(res, 201, {
            id: newRest.restaurantId,
            name: newRest.restaurantName,
            email: newRest.email,
            role: 'RESTAURANT',
          });
        }

        if (url === '/api/auth/driver/register' && req.method === 'POST') {
          const body = await parseBody(req);
          const newDriver = {
            driverId: drivers.length + 1,
            fullName: body.fullName || 'Tài xế mới',
            email: body.email,
            phoneNumber: body.phoneNumber || '0909876543',
            licensePlate: body.licensePlate || '29A-99999',
            isOnline: false,
            isBusy: false,
            currentLatitude: 21.0285,
            currentLongitude: 105.8542,
          };
          drivers.push(newDriver);
          return sendJson(res, 201, {
            id: newDriver.driverId,
            name: newDriver.fullName,
            email: newDriver.email,
            role: 'DRIVER',
          });
        }

        // 2. Restaurants
        if (url === '/api/restaurants' && req.method === 'GET') {
          return sendJson(res, 200, restaurants);
        }

        const restItemsMatch = url.match(/^\/api\/restaurants\/(\d+)\/items$/);
        if (restItemsMatch && req.method === 'GET') {
          const restId = Number(restItemsMatch[1]);
          const items = foodItems.filter((i) => i.restaurantId === restId && i.isAvailable);
          return sendJson(res, 200, items);
        }

        const restOrdersMatch = url.match(/^\/api\/restaurants\/(\d+)\/orders$/);
        if (restOrdersMatch && req.method === 'GET') {
          const restId = Number(restOrdersMatch[1]);
          const restOrders = orders.filter((o) => o.restaurantId === restId);
          return sendJson(res, 200, restOrders);
        }

        // 3. Payment methods
        if (url === '/api/payment-methods' && req.method === 'GET') {
          return sendJson(res, 200, paymentMethods);
        }

        // 4. Order preview
        if (url === '/api/orders/preview' && req.method === 'POST') {
          const body = await parseBody(req);
          const rest = restaurants.find((r) => r.restaurantId === body.restaurantId) || restaurants[0];
          const { distanceKm, shippingFee } = calcShipping(
            rest.latitude,
            rest.longitude,
            body.deliveryLatitude || 20.9806,
            body.deliveryLongitude || 105.7876
          );

          let subtotal = 0;
          for (const it of body.items || []) {
            const fi = foodItems.find((f) => f.itemId === it.itemId);
            if (fi) subtotal += fi.price * it.quantity;
          }

          return sendJson(res, 200, {
            distanceKm,
            shippingFee,
            subtotal,
            totalAmount: subtotal + shippingFee,
          });
        }

        // 5. Orders CRUD
        if (url === '/api/orders' && req.method === 'POST') {
          const body = await parseBody(req);
          const rest = restaurants.find((r) => r.restaurantId === body.restaurantId) || restaurants[0];
          const cust = customers.find((c) => c.customerId === body.customerId) || customers[0];

          const { distanceKm, shippingFee } = calcShipping(
            rest.latitude,
            rest.longitude,
            body.deliveryLatitude || 20.9806,
            body.deliveryLongitude || 105.7876
          );

          const detailedItems = (body.items || []).map((it) => {
            const fi = foodItems.find((f) => f.itemId === it.itemId);
            return {
              itemId: it.itemId,
              itemName: fi ? fi.itemName : `Món #${it.itemId}`,
              price: fi ? fi.price : 50000,
              quantity: it.quantity,
            };
          });

          const subtotal = detailedItems.reduce((s, i) => s + i.price * i.quantity, 0);
          const newOrder = {
            orderId: nextOrderId++,
            customerId: body.customerId,
            customerName: cust.fullName,
            restaurantId: body.restaurantId,
            restaurantName: rest.restaurantName,
            distanceKm,
            shippingFee,
            totalAmount: subtotal + shippingFee,
            orderStatus: 'PENDING',
            deliveryAddress: body.deliveryAddress,
            deliveryLatitude: body.deliveryLatitude,
            deliveryLongitude: body.deliveryLongitude,
            notes: body.notes || '',
            createdAt: new Date().toISOString(),
            items: detailedItems,
            payment: {
              paymentId: nextOrderId * 10,
              orderId: nextOrderId - 1,
              methodCode: 'COD',
              paymentStatus: 'PENDING',
              amount: subtotal + shippingFee,
              transactionCode: `TXN-${nextOrderId - 1}`,
            },
          };
          orders.unshift(newOrder);
          return sendJson(res, 201, newOrder);
        }

        const orderDetailMatch = url.match(/^\/api\/orders\/(\d+)$/);
        if (orderDetailMatch && req.method === 'GET') {
          const ordId = Number(orderDetailMatch[1]);
          const found = orders.find((o) => o.orderId === ordId);
          if (found) return sendJson(res, 200, found);
          return sendJson(res, 404, { message: 'Không tìm thấy đơn hàng' });
        }

        const orderStatusMatch = url.match(/^\/api\/orders\/(\d+)\/status$/);
        if (orderStatusMatch && req.method === 'PATCH') {
          const ordId = Number(orderStatusMatch[1]);
          const body = await parseBody(req);
          const found = orders.find((o) => o.orderId === ordId);
          if (found) {
            found.orderStatus = body.newStatus;
            return sendJson(res, 200, found);
          }
          return sendJson(res, 404, { message: 'Đơn hàng không tồn tại' });
        }

        const orderCancelMatch = url.match(/^\/api\/orders\/(\d+)\/cancel$/);
        if (orderCancelMatch && req.method === 'PATCH') {
          const ordId = Number(orderCancelMatch[1]);
          const found = orders.find((o) => o.orderId === ordId);
          if (found) {
            found.orderStatus = 'CANCELLED';
            if (found.payment && found.payment.paymentStatus === 'PAID') {
              found.payment.paymentStatus = 'REFUNDED';
            }
            return sendJson(res, 200, found);
          }
          return sendJson(res, 404, { message: 'Đơn hàng không tồn tại' });
        }

        // 6. Customer orders
        const custOrdersMatch = url.match(/^\/api\/customers\/(\d+)\/orders$/);
        if (custOrdersMatch && req.method === 'GET') {
          const custId = Number(custOrdersMatch[1]);
          const userOrders = orders.filter((o) => o.customerId === custId);
          return sendJson(res, 200, userOrders);
        }

        // 7. Driver APIs
        const drvOnlineMatch = url.match(/^\/api\/drivers\/(\d+)\/online$/);
        if (drvOnlineMatch && req.method === 'PATCH') {
          const drvId = Number(drvOnlineMatch[1]);
          const body = await parseBody(req);
          const drv = drivers.find((d) => d.driverId === drvId);
          if (drv) drv.isOnline = body.online;
          return sendJson(res, 200, drv);
        }

        if (url === '/api/drivers/deliveries/available' && req.method === 'GET') {
          // READY_FOR_PICKUP and not in deliveries
          const activeDeliveryOrderIds = deliveries
            .filter((d) => d.deliveryStatus === 'ASSIGNED' || d.deliveryStatus === 'PICKED_UP')
            .map((d) => d.orderId);
          const available = orders.filter(
            (o) => o.orderStatus === 'READY_FOR_PICKUP' && !activeDeliveryOrderIds.includes(o.orderId)
          );
          return sendJson(res, 200, available);
        }

        const drvActiveMatch = url.match(/^\/api\/drivers\/(\d+)\/active-delivery$/);
        if (drvActiveMatch && req.method === 'GET') {
          const drvId = Number(drvActiveMatch[1]);
          const active = deliveries.filter(
            (d) =>
              d.driverId === drvId &&
              (d.deliveryStatus === 'ASSIGNED' || d.deliveryStatus === 'PICKED_UP')
          );
          return sendJson(res, 200, active);
        }

        // 8. Deliveries accept & update status
        const acceptDeliveryMatch = url.match(/^\/api\/deliveries\/(\d+)\/accept$/);
        if (acceptDeliveryMatch && req.method === 'POST') {
          const ordId = Number(acceptDeliveryMatch[1]);
          const body = await parseBody(req);
          const existing = deliveries.find(
            (d) => d.orderId === ordId && (d.deliveryStatus === 'ASSIGNED' || d.deliveryStatus === 'PICKED_UP')
          );
          if (existing) {
            return sendJson(res, 409, { message: 'Đơn hàng đã được tài xế khác nhận!' });
          }

          const newDelivery = {
            deliveryId: deliveries.length + 1,
            orderId: ordId,
            driverId: body.driverId,
            deliveryStatus: 'ASSIGNED',
            pickupTime: null,
          };
          deliveries.push(newDelivery);

          const ord = orders.find((o) => o.orderId === ordId);
          if (ord) ord.orderStatus = 'DELIVERING';

          const drv = drivers.find((d) => d.driverId === body.driverId);
          if (drv) drv.isBusy = true;

          return sendJson(res, 201, newDelivery);
        }

        const deliveryStatusMatch = url.match(/^\/api\/deliveries\/(\d+)\/status$/);
        if (deliveryStatusMatch && req.method === 'PATCH') {
          const delId = Number(deliveryStatusMatch[1]);
          const body = await parseBody(req);
          const del = deliveries.find((d) => d.deliveryId === delId);
          if (del) {
            del.deliveryStatus = body.newStatus;
            const ord = orders.find((o) => o.orderId === del.orderId);
            if (ord) {
              if (body.newStatus === 'PICKED_UP') {
                ord.orderStatus = 'DELIVERING';
                del.pickupTime = new Date().toISOString();
              } else if (body.newStatus === 'DELIVERED') {
                ord.orderStatus = 'COMPLETED';
                if (ord.payment && ord.payment.methodCode === 'COD') {
                  ord.payment.paymentStatus = 'PAID';
                }
                const drv = drivers.find((d) => d.driverId === del.driverId);
                if (drv) drv.isBusy = false;
              }
            }
            return sendJson(res, 200, del);
          }
          return sendJson(res, 404, { message: 'Không tìm thấy chuyến giao' });
        }

        // 9. Payment
        if (url === '/api/payments' && req.method === 'POST') {
          const body = await parseBody(req);
          const ord = orders.find((o) => o.orderId === body.orderId);
          if (ord) {
            ord.payment = {
              paymentId: Date.now(),
              orderId: body.orderId,
              methodCode: body.methodCode,
              paymentStatus: 'PENDING',
              amount: ord.totalAmount,
              transactionCode: `TXN-${body.orderId}`,
            };
            return sendJson(res, 201, ord.payment);
          }
          return sendJson(res, 404, { message: 'Đơn hàng không tồn tại' });
        }

        if (url === '/api/payments/webhook/vietqr' && req.method === 'POST') {
          const body = await parseBody(req);
          const ord = orders.find((o) => o.orderId === body.orderId);
          if (ord && ord.payment) {
            ord.payment.paymentStatus = 'PAID';
            ord.payment.transactionCode = body.transactionCode;
            return sendJson(res, 200, ord.payment);
          }
          return sendJson(res, 404, { message: 'Đơn hàng không tồn tại' });
        }

        // 10. Rating
        const ratingMatch = url.match(/^\/api\/order-items\/(\d+)\/(\d+)\/rating$/);
        if (ratingMatch && req.method === 'POST') {
          const ordId = Number(ratingMatch[1]);
          const itemId = Number(ratingMatch[2]);
          const body = await parseBody(req);
          const newRating = {
            ratingId: ratings.length + 1,
            orderId: ordId,
            itemId,
            ratingScore: body.ratingScore,
            comment: body.comment,
            createdAt: new Date().toISOString(),
          };
          ratings.push(newRating);
          return sendJson(res, 201, newRating);
        }

        return next();
      });
    },
  };
}
