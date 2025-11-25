package com.facushop.service;

import com.facushop.domain.Order;
import com.facushop.domain.OrderStatus;
import com.facushop.repository.OrderRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.facushop.domain.OrderItem;
import com.facushop.domain.Product;
import com.facushop.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient = new PaymentClient(); // Cliente de MP para buscar pagos
    private final ProductRepository productRepository;

    @Transactional
    public void processPaymentNotification(String paymentId) throws Exception {
        // 1. Con el ID del pago, buscamos el pago completo en la API de MP
        //    (Esto también es una medida de seguridad)
        Payment payment = paymentClient.get(Long.parseLong(paymentId));

        // 2. Verificamos el estado del pago
        if (payment.getStatus().equals("approved")) {

            // 3. Obtenemos el ID de nuestro pedido (el que guardamos en 'external_reference')
            String orderIdString = payment.getExternalReference();
            Long orderId = Long.parseLong(orderIdString);

            // 4. Buscamos el pedido en nuestra BD
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));

            // 5. ¡ACTUALIZAMOS EL ESTADO!
            if (order.getStatus() == OrderStatus.CART) {
                // --- LÓGICA DE DESCUENTO DE STOCK ---
                for (OrderItem item : order.getItems()) {
                    // (OJO: Esto puede necesitar @EntityGraph en OrderRepository para cargar los items)
                    Product product = item.getProduct();

                    if (product.getStock() < item.getQuantity()) {
                        // Esto es grave, el stock cambió entre el checkout y el pago
                        System.err.println("¡ALERTA! Stock insuficiente para " + product.getTitle() + " en pedido " + orderId);
                        // (Aquí podrías cancelar el pago, pero por ahora solo lo notificamos y continuamos)
                    }

                    int newStock = product.getStock() - item.getQuantity();
                    product.setStock(newStock);
                    productRepository.save(product);
                }
                // --- FIN DE LÓGICA DE STOCK ---
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                System.out.println(">>> Pedido " + orderId + " actualizado a PAID.");
            }
        }
    }
}