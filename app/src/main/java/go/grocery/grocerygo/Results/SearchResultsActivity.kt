package go.grocery.grocerygo.Results

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import go.grocery.grocerygo.R
import com.google.firebase.firestore.FirebaseFirestore
import go.grocery.grocerygo.SelectGroceries.SummaryListItem
import kotlinx.android.synthetic.main.activity_search_results.*

class SearchResultsActivity : AppCompatActivity() {

    data class Supermarket(val name : String, val totalPrice : Double, val items : ArrayList<SummaryListItem>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)
    }

    fun onDataReceived(){
        progressBar.visibility = View.GONE
        val supermarket = getItems(intent.getStringArrayListExtra("shopping_list"))
        val bundle = Bundle()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        bundle.putString("supermarket_name", supermarket.name)
        bundle.putDouble("total_price", supermarket.totalPrice)
        bundle.putSerializable("grocery_items", supermarket.items)
        val fragment = SearchResultsFragment()
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.search_activity_content, fragment)
        fragmentTransaction.commit()
    }

    fun getItems(names : Collection<String>) : Supermarket {
        val db = FirebaseFirestore.getInstance()

        val walmartItems = HashMap<String, Double>()
        val zehrsItems = HashMap<String, Double>()
        val nofrillsItems = HashMap<String, Double>()

        db.collection("walmart")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (names.contains(document.id) ) {
                        walmartItems[document.getString("name") as String] = document.getDouble("price") as Double
                    }
                }
            }

        db.collection("zehrs")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (names.contains(document.id) ) {
                        zehrsItems[document.getString("name") as String] = document.getDouble("price") as Double
                    }
                }
            }

        db.collection("nofrills")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (names.contains(document.id) ) {
                        nofrillsItems[document.getString("name") as String] = document.get("price") as Double
                    }
                }
            }

        var walmart = 0.0
        var zehrs = 0.0
        var nofrills = 0.0

        for ((k, v) in walmartItems)
            walmart += v
        for ((k, v) in zehrsItems)
            zehrs += v
        for ((k, v) in nofrillsItems)
            nofrills += v

        val summaryList = ArrayList<SummaryListItem>()

        if (walmart <= zehrs && walmart <= nofrills) {
            for ((k,v) in walmartItems) {
                summaryList.add(SummaryListItem(k, v.toString()))
            }
            return Supermarket("Walmart", walmart, summaryList)
        }
        if (zehrs <= walmart && zehrs <= nofrills) {
            for ((k,v) in zehrsItems) {
                summaryList.add(SummaryListItem(k, v.toString()))
            }
            return Supermarket("Zehrs", zehrs, summaryList)
        }
        for ((k,v) in nofrillsItems) {
            summaryList.add(SummaryListItem(k, v.toString()))
        }
        return Supermarket("No Frills", nofrills, summaryList)

    }
}
