package activitytest.example.com.test01;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by 魏昊妤 on 2017/6/6.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context mContext;
    private List<Image> mImageList;
    private MyItemClickListener listener;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView image;
        TextView imageName;
        TextView imageDes;

        public ViewHolder(View view,  final MyItemClickListener mListener){
            super(view);
            cardView =(CardView) view;
            image = (ImageView) view.findViewById(R.id.image);
            imageName = (TextView) view.findViewById(R.id.image_name);
            imageDes = (TextView) view.findViewById(R.id.image_description);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(v);
                }
            });
        }
    }

    public ImageAdapter(List<Image> imageList){
        mImageList = imageList;
    }

    @Override
    public  ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        Image image = mImageList.get(position);
        holder.imageName.setText(image.getName());
        holder.imageDes.setText(image.getDescription());
        Glide.with(mContext).load(image.getImageId()).into(holder.image);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount(){
        return mImageList.size();
    }

    /**
     * 为Adapter暴露一个Item点击监听的公开方法
     *
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener) {
        this.listener = listener;
    }

}
/**
 * 回调接口
 */
interface MyItemClickListener {
    void onItemClick(View view);
}
