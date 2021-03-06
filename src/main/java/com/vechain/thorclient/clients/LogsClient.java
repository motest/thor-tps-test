package com.vechain.thorclient.clients;

import com.vechain.thorclient.clients.base.AbstractClient;
import com.vechain.thorclient.core.model.blockchain.*;
import com.vechain.thorclient.core.model.clients.Address;
import com.vechain.thorclient.core.model.exception.ClientArgumentException;
import com.vechain.thorclient.core.model.exception.ClientIOException;
import com.vechain.thorclient.utils.Prefix;

import java.util.ArrayList;
import java.util.HashMap;

import static com.vechain.thorclient.clients.base.AbstractClient.Path.PostFilterEventsLogPath;

/**
 * Get log generated by contract with event filter or transfer logs.
 */
public class LogsClient extends AbstractClient {

	/**
	 * Get events log from a address.
	 * 
	 * @param filter
	 *            required {@link EventFilter} a filter for the events log.
	 * @param order
	 *            optional {@link Order} a time order for the events list.
	 * @param address
	 *            optional {@link Address} a address which has events log.
	 * @return array of {@link FilteredEvent}, could be null. throws ClientIOException network error or request invalid.
	 */
	@Deprecated
	public static ArrayList<?> filterEvents(EventFilter filter, Order order, Address address) throws ClientIOException {
		if (filter == null) {
			throw ClientArgumentException.exception("filter is null");
		}
		HashMap<String, String> queryParams = new HashMap<>();
		if (order != null) {
			queryParams.put("order", order.getValue());
		}
		if (address != null) {
			queryParams.put("address", address.toHexString(Prefix.ZeroLowerX));
		}

		return sendPostRequest( PostFilterEventsLogPath, null, queryParams, filter,
				ArrayList.class);

	}

	/**
	 * Get transfer logs by order
	 * 
	 * @param filter
	 *            required {@link TransferFilter} filter.
	 * @param order
	 *            optional {@link Order} the result order.
	 * @return array of {@link FilteredTransfer} could be null.
	 * @throws ClientIOException
	 *             network error
	 */
	@Deprecated
	public static ArrayList<?> filterTransferLogs(TransferFilter filter, Order order) throws ClientIOException {
		if (filter == null) {
			throw ClientArgumentException.exception("filter is null");
		}
		HashMap<String, String> queryParams = new HashMap<>();
		if (order != null) {
			queryParams.put("order", order.getValue());
		}

		return sendPostRequest(Path.PostFilterTransferLogPath, null, queryParams, filter,
				ArrayList.class);

	}

    /**
     * Get filtered log events.
     * @param logFilter {@link LogFilter}
     * @return ArrayList {@link FilteredLogEvent}
     * @throws ClientIOException
     */
    public static ArrayList<FilteredLogEvent> getFilteredLogEvents(LogFilter logFilter) throws ClientIOException {
        if (logFilter == null) {
            throw ClientArgumentException.exception( "logFilter is null" );
        }
        return sendPostRequest( Path.PostFilterEventsLogPath, null, null, logFilter,
                new ArrayList<FilteredLogEvent>().getClass());
    }

    /**
     * Get filtered transferred log events.
     * @param transferredFilter {@link TransferredFilter}
     * @return ArrayList of {@link FilteredTransferEvent}
     * @throws ClientIOException
     */
    public static ArrayList<FilteredTransferEvent> getFilteredTransferLogs(TransferredFilter transferredFilter) throws
            ClientIOException{
        if (transferredFilter == null){
            throw ClientArgumentException.exception( "transferredFilter is null" );
        }
        return sendPostRequest( Path.PostFilterTransferLogPath, null, null, transferredFilter,
                new ArrayList<FilteredTransferEvent>().getClass());
    }


}
